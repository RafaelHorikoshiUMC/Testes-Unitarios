package com.pfc.thindesk.controller;

import com.pfc.thindesk.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import com.pfc.thindesk.email.dtos.RecoverDto;
import com.pfc.thindesk.PerfilMatchDTO;
import com.pfc.thindesk.entity.*;
import com.pfc.thindesk.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private JogoService jogoService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PerfilService perfilService;
    @Autowired
    private GrupoService grupoService;
    @Autowired
    private SugestaoDeJogoService sugestaoDeJogoService;
    @Autowired
    private DepoimentoService depoimentoService;
    @Autowired
    private DecisaoMatchService decisaoMatchService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Página da dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<DecisaoMatch> todasDecisoes = decisaoMatchService.listarTodasDecisaoMatch();
        model.addAttribute("QuantidadeDecisoes", todasDecisoes.size());
        List<Depoimento> todosDepoimentos = depoimentoService.listarTodosDepoimentos();
        model.addAttribute("QuantidadeDepoimentos", todosDepoimentos.size());
        List<Grupo> todosGrupos = grupoService.listarTodosGrupos();
        model.addAttribute("QuantidadeGrupos", todosGrupos.size());
        List<Jogo> todosJogos = jogoService.listarTodosJogos();
        model.addAttribute("QuantidadeJogos", todosJogos.size());

        List<Perfil> todosPerfis = perfilService.listarTodosPerfis();
        model.addAttribute("QuantidadePerfis", todosPerfis.size());

        List<SugestaoDeJogo> todasSugestoes = sugestaoDeJogoService.listarTodasSugestaoDeJogo();
        model.addAttribute("QuantidadeSugestoes", todasSugestoes.size());
        return "dashboard";
    }

    // Página inicial
    @GetMapping("/inicial")
    public String inicial() {
        return "inicial";
    }

    // Página das politicas de Privacidade
    @GetMapping("/politicasPrivacidade")
    public String politicasPrivacidade() {
        return "politicasPrivacidade";
    }

    // Página de login (já configurada pelo Spring Security)
    @GetMapping("/login")
    public String login() {
        return "login"; // Página de login
    }

    // Redireciona para a página inicial
    @GetMapping("/")
    public String home() {
        return "inicial";
    }

    // Lista todos os jogos
    @GetMapping("/jogos")
    public String listarJogos(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String termo, Model model) {
        Pageable pageable = PageRequest.of(page, 25);
        Page<Jogo> jogosPage = jogoService.buscarJogosPaginados(termo, pageable);

        model.addAttribute("jogos", jogosPage.getContent());
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", jogosPage.getTotalPages());
        model.addAttribute("termo", termo);

        return "jogos";
    }

    // Exibi o cadastro para registrar um jogo
    @GetMapping("/jogos/novo")
    public String novoCadastroJogo(Model model) {

        model.addAttribute("jogo", new Jogo());
        return "novoJogo";
    }

    // Exibi o cadastro para editar um jogo existente
    @GetMapping("/admin/editar/{id}")
    public String editarCadastroJogo(@PathVariable("id") String id, Model model) {
        Jogo jogo = jogoService.buscarJogoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));
        model.addAttribute("jogo", jogo);
        return "editarJogo";
    }

    // Salva um novo jogo
    @PostMapping("/jogos/salvar")
    public String salvarjogo(@ModelAttribute Jogo jogo, RedirectAttributes redirectAttributes) {
        jogoService.criarJogo(jogo);
        redirectAttributes.addFlashAttribute("msgSucesso", "Jogo cadastrado com sucesso!");
        return "redirect:/jogos";
    }

    // Deleta um jogo
    @PostMapping("/admin/jogos/deletar")
    public String deletarJogo(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        boolean deletado = jogoService.deletarJogo(id);
        if (deletado) {
            redirectAttributes.addFlashAttribute("msgSucesso", "Jogo deletado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("msgErro", "Jogo não encontrado.");
        }
        return "redirect:/jogos";
    }


    // Atualiza um jogo
    @PostMapping("/jogos/atualizar/{id}")
    public String atualizarJogo(@PathVariable("id") String id, @ModelAttribute("jogo") Jogo jogo) {
        jogo.setId(id);
        jogoService.atualizarJogo(id, jogo);
        return "redirect:/jogos";
    }

    // Exibi o cadastro para registrar um usuario
    @GetMapping("/usuarios/novo")
    public String novoCadastroUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "novoUsuario";
    }

    // Salva um novo usuario
    @PostMapping("/usuarios/salvar")
    public String salvarUsuario(@ModelAttribute Usuario usuario, Model model, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.criarUsuario(usuario);
            redirectAttributes.addFlashAttribute("msgSucesso", "Usuario cadastrado com sucesso!");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("erro", e.getMessage());
            return "novoUsuario";
        }
    }

    // Página do administrador
    @GetMapping("/admin/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarUsuarios(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String termo, Model model) {

        Pageable pageable = PageRequest.of(page, 25);
        Page<Usuario> usuarios;

        if (termo != null && !termo.trim().isEmpty()) {
            usuarios = usuarioRepository
                    .findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(termo, termo, pageable);
        } else {
            usuarios = usuarioRepository.findAll(pageable);
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("termo", termo); // preserva o termo no input
        return "admin/usuarios"; // nome da sua página HTML
    }


    // Deleta usuarios se for administrador
    @PostMapping("/admin/deletar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletarUsuario(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        usuarioService.deletarUsuario(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário deletado com sucesso!");
        return "redirect:/admin/usuarios";
    }

    // Página de erro
    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "acesso-negado";
    }

    // Lista todos os grupos
    @GetMapping("/grupos")
    public String listarGrupos(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, RedirectAttributes redirectAttributes) {

        Optional<Perfil> meuPerfil = perfilService.listarPerfisDoUsuario().stream().findFirst();

        if (meuPerfil.isEmpty()) {
            redirectAttributes.addFlashAttribute("msgErro", "Você precisa criar um perfil antes de visualizar ou interagir com os grupos.");
            return "redirect:/perfis";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Grupo> gruposPage = grupoService.listarTodosGruposPaginados(pageable);

        String emailUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(emailUsuarioLogado).orElse(null);

        model.addAttribute("grupos", gruposPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", gruposPage.getTotalPages());
        model.addAttribute("perfilLogado", perfilLogado);

        return "grupos";
    }

    // Exibi o cadastro para registrar um grupo
    @GetMapping("/grupos/novo")
    public String novoCadastroGrupo(Model model) {
        List<Jogo> jogos = jogoService.listarTodosJogos(); // Sua forma de buscar todos os jogos
        model.addAttribute("jogos", jogos);
        model.addAttribute("grupo", new Grupo());
        return "novoGrupo";
    }

    // Exibi o cadastro para editar um grupo existente
    @GetMapping("/grupos/editar/{id}")
    public String editarCadastroGrupo(@PathVariable("id") String id, Model model) {
        Grupo grupo = grupoService.buscarGrupoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));
        List<Jogo> jogos = jogoService.listarTodosJogos(); // Sua forma de buscar todos os jogos
        model.addAttribute("jogos", jogos);
        model.addAttribute("grupo", grupo);
        return "editarGrupo";
    }

    // Salva um novo grupo
    @PostMapping("/grupos/salvar")
    public String salvarGrupo(@ModelAttribute Grupo grupo, RedirectAttributes redirectAttributes) {
        grupoService.criarGrupo(grupo);
        redirectAttributes.addFlashAttribute("msgSucesso", "Grupo criado com sucesso!");
        return "redirect:/grupos";
    }

    // Deleta um grupo
    @PostMapping("/grupos/deletar")
    public String deletarGrupo(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("msgSucesso", "Grupo deletado com sucesso!");
        grupoService.deletarGrupo(id);
        return "redirect:/grupos";
    }

    // Atualiza um grupo
    @PostMapping("/grupos/atualizar/{id}")
    public String atualizarGrupo(@PathVariable("id") String id, @ModelAttribute("grupo") Grupo grupo,  RedirectAttributes redirectAttributes) {
        grupo.setId(id);
        grupoService.atualizarGrupo(id, grupo);
        redirectAttributes.addFlashAttribute("msgSucesso", "Grupo atualizado com sucesso!");
        return "redirect:/grupos";
    }

    // Entrar no grupo
    @PostMapping("/grupos/{id}/entrar")
    public String entrarNoGrupo(@PathVariable String id, RedirectAttributes ra) {
        try {
            grupoService.entrarNoGrupo(id);
            ra.addFlashAttribute("msgSucesso", "Você entrou no grupo com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("msgErro", e.getMessage());
        }
        return "redirect:/grupos";
    }

    // Lista todos os perfis
    @GetMapping("/perfis")
    public String listarPerfis(Model model) {
        // Obtém apenas os perfis do usuário autenticado
        List<Perfil> perfis = perfilService.listarPerfisDoUsuario();
        model.addAttribute("perfis", perfis);

        String fragment = "perfis :: content";
        log.info("Carregando fragmento: {}", fragment); // Log para depuração
        model.addAttribute("content", fragment);

        return "perfis";
    }

    // Exibi o cadastro para registrar um perfil
    @GetMapping("/perfis/novo")
    public String novoCadastroPerfil(Model model) {
        model.addAttribute("perfil", new Perfil());
        List<Jogo> jogos = jogoService.listarTodosJogos(); // Sua forma de buscar todos os jogos
        model.addAttribute("jogos", jogos);
        // Também passe as seeds dos avatares se for o caso
        model.addAttribute("seeds", List.of("1", "2", "3", "4", "5", "6", "7", "9", "10", "11", "12"));
        return "novoPerfil";
    }

    // Exibi o cadastro para editar um perfil existente
    @GetMapping("/perfis/editar/{id}")
    public String editarCadastroPerfil(@PathVariable("id") String id, Model model) {
        Perfil perfil = perfilService.buscarPerfilPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));
        model.addAttribute("perfil", perfil);
        List<Jogo> jogos = jogoService.listarTodosJogos(); // Sua forma de buscar todos os jogos
        model.addAttribute("jogos", jogos);
        model.addAttribute("seeds", List.of("1", "2", "3", "4", "5", "6", "7", "9", "10", "11", "12"));
        return "editarPerfil";
    }

    // Salva um novo perfil
    @PostMapping("/perfis/salvar")
    public String salvarPerfil(@ModelAttribute Perfil perfil, RedirectAttributes redirectAttributes) {
        perfilService.criarPerfil(perfil);
        redirectAttributes.addFlashAttribute("msgSucesso", "Perfil criado com sucesso!");
        return "redirect:/perfis";
    }

    // Deleta um perfil
    @PostMapping("/perfis/deletar")
    public String deletarPerfil(@RequestParam("id") String id) {
        perfilService.deletarPerfil(id);
        return "redirect:/perfis";
    }

    // Atualiza um perfil
    @PostMapping("/perfis/atualizar/{id}")
    public String atualizarPerfil(@PathVariable("id") String id, @ModelAttribute("perfil") Perfil perfil, RedirectAttributes redirectAttributes) {
        perfil.setId(id);
        perfilService.atualizarPerfil(id, perfil);
        redirectAttributes.addFlashAttribute("msgSucesso", "Perfil atualizado com sucesso!");
        return "redirect:/perfis";
    }

    // Lista todas as Sugestões De Jogos
    @GetMapping("/sugestoesDeJogos")
    public String listarSugestoesDeJogos(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, RedirectAttributes redirectAttributes) {

        Optional<Perfil> meuPerfil = perfilService.listarPerfisDoUsuario().stream().findFirst();

        if (meuPerfil.isEmpty()) {
            redirectAttributes.addFlashAttribute("msgErro", "Você precisa criar um perfil antes de visualizar ou interagir com as sugestões de jogos.");
            return "redirect:/perfis";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<SugestaoDeJogo> sugestoesPage = sugestaoDeJogoService.listarTodasSugestoesPaginadas(pageable);

        String emailUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(emailUsuarioLogado).orElse(null);

        model.addAttribute("sugestoesDeJogos", sugestoesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sugestoesPage.getTotalPages());
        model.addAttribute("perfilLogado", perfilLogado);

        return "sugestoesDeJogos";
    }


    // Exibi o cadastro para registrar uma Sugestão De Jogo
    @GetMapping("/sugestoesDeJogos/novo")
    public String novaSugestaoDeJogo(Model model) {
        model.addAttribute("sugestaoDeJogo", new SugestaoDeJogo());
        return "novaSugestaoDeJogo";
    }

    // Exibi o cadastro para editar uma Sugestão De Jogo
    @GetMapping("/sugestoesDeJogos/editar/{id}")
    public String editarSugestaoDeJogo(@PathVariable("id") String id, Model model) {
        SugestaoDeJogo sugestaoDeJogo = sugestaoDeJogoService.buscarSugestaoDeJogoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Sugestao De Jogo não encontrado: " + id));
        model.addAttribute("sugestaoDeJogo", sugestaoDeJogo);
        return "editarSugestaoDeJogo";
    }

    // Salva uma nova Sugestão De Jogo
    @PostMapping("/sugestoesDeJogos/salvar")
    public String salvarSugestaoDeJogo(@ModelAttribute SugestaoDeJogo sugestaoDeJogo, RedirectAttributes redirectAttributes) {
        sugestaoDeJogoService.criarSugestaoDeJogo(sugestaoDeJogo);
        redirectAttributes.addFlashAttribute("msgSucesso", "Sugestão De Jogo criada com sucesso!");
        return "redirect:/sugestoesDeJogos";
    }

    // Deleta uma Sugestão De Jogo
    @PostMapping("/sugestoesDeJogos/deletar")
    public String deletarSugestaoDeJogo(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        sugestaoDeJogoService.deletarSugestaoDeJogo(id);
        redirectAttributes.addFlashAttribute("msgSucesso", "Sugestão De Jogo deletada com sucesso!");
        return "redirect:/sugestoesDeJogos";
    }

    // Atualiza uma Sugestão De Jogo
    @PostMapping("/sugestoesDeJogos/atualizar/{id}")
    public String atualizarSugestaoDeJogo(@PathVariable("id") String id, @ModelAttribute("sugestoes") SugestaoDeJogo sugestaoDeJogo, RedirectAttributes redirectAttributes) {
        sugestaoDeJogo.setId(id);
        sugestaoDeJogoService.atualizarSugestaoDeJogo(id, sugestaoDeJogo);
        redirectAttributes.addFlashAttribute("msgSucesso", "Sugestão De Jogo atualizada com sucesso!");
        return "redirect:/sugestoesDeJogos";
    }

    // Lista todos os Depoimentos
    @GetMapping("/depoimentos")
    public String listarDepoimentos(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, Model model, RedirectAttributes redirectAttributes) {
        Optional<Perfil> meuPerfil = perfilService.listarPerfisDoUsuario().stream().findFirst();

        if (meuPerfil.isEmpty()) {
            redirectAttributes.addFlashAttribute("msgErro", "Você precisa criar um perfil antes de visualizar ou interagir com os depoimentos.");
            return "redirect:/perfis";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Depoimento> depoimentosPage = depoimentoService.listarTodosDepoimentosPaginados(pageable);

        String emailUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Perfil perfilLogado = perfilService.buscarPerfilDoUsuarioLogado(emailUsuarioLogado).orElse(null);

        model.addAttribute("depoimentos", depoimentosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", depoimentosPage.getTotalPages());
        model.addAttribute("perfilLogado", perfilLogado);

        return "depoimentos";
    }


    // Exibi o cadastro para registrar um Depoimento
    @GetMapping("/depoimentos/novo")
    public String novoDepoimento(Model model) {
        model.addAttribute("depoimento", new Depoimento());
        return "novoDepoimento";
    }

    // Exibi o cadastro para editar um  Depoimento
    @GetMapping("/depoimentos/editar/{id}")
    public String editarDepoimento(@PathVariable("id") String id, Model model) {
        Depoimento depoimento = depoimentoService.buscarDepoimentoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Depoimento não encontrado: " + id));
        model.addAttribute("depoimento", depoimento);
        return "editarDepoimento";
    }

    // Salva um novo Depoimento
    @PostMapping("/depoimentos/salvar")
    public String salvarDepoimento(@ModelAttribute Depoimento Depoimento, RedirectAttributes redirectAttributes) {
        depoimentoService.criarDepoimento(Depoimento);
        redirectAttributes.addFlashAttribute("msgSucesso", "Depoimento criado com sucesso!");
        return "redirect:/depoimentos";
    }

    // Deleta um Depoimento
    @PostMapping("/depoimentos/deletar")
    public String deletarDepoimento(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        depoimentoService.deletarDepoimento(id);
        redirectAttributes.addFlashAttribute("msgSucesso", "Depoimento deletado com sucesso!");
        return "redirect:/depoimentos";
    }

    // Atualiza um Depoimento
    @PostMapping("/depoimentos/atualizar/{id}")
    public String atualizarDepoimento(@PathVariable("id") String id, @ModelAttribute("depoimentos") Depoimento depoimento, RedirectAttributes redirectAttributes) {
        depoimento.setId(id);
        depoimentoService.atualizarDepoimento(id, depoimento);
        redirectAttributes.addFlashAttribute("msgSucesso", "Depoimento atualizado com sucesso!");
        return "redirect:/depoimentos";
    }

    // Exibe o formulário para digitar o e-mail de recuperação
    @GetMapping("/recuperar")
    public String mostrarFormularioRecuperarSenha() {
        return "recuperar"; // página para digitar o email
    }

    // Processa o envio do e-mail de recuperação de senha
    @PostMapping("/recuperar")
    public String processarPedidoRecuperarSenha(@RequestParam("email") String email, Model model) {
        try {
            usuarioService.sendEmailRecoverPassword(new RecoverDto(email)); // envia o e-mail com link
            return "redirect:/recuperar-enviado"; // redireciona para página de confirmação
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage()); // exibe erro na mesma página
            return "recuperar";
        }
    }

    // Exibe a confirmação de que o e-mail foi enviado
    @GetMapping("/recuperar-enviado")
    public String paginaEmailEnviado() {
        return "recuperar-enviado";
    }

    // Exibe o formulário para o usuário atualizar a senha usando o token
    @GetMapping("/atualizar-senha/{token}")
    public String mostrarFormularioAtualizarSenha(@PathVariable String token, Model model) {
        model.addAttribute("token", token); // passa o token para o formulário
        return "atualizar-senha";
    }

    // Processa a atualização da senha usando o token
    @PostMapping("/atualizar-senha/{token}")
    public String atualizarSenha(
            @PathVariable String token,
            @RequestParam("senha") String senha,
            Model model
    ) {
        try {
            usuarioService.updatePassword(token, senha); // atualiza a senha
            return "redirect:/senha-atualizada"; // redireciona para confirmação
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage()); // mostra erro, permanece na tela
            model.addAttribute("token", token);
            return "recuperar-senha";
        }
    }

    // Exibe confirmação de que a senha foi atualizada com sucesso
    @GetMapping("/senha-atualizada")
    public String paginaSenhaAtualizada() {
        return "senha-atualizada";
    }

    // Mostra os perfis compativeis
    @GetMapping("/perfis/compativeis")
    public String mostrarPerfisCompativeis(Model model, RedirectAttributes redirectAttributes) {
        Optional<Perfil> meuPerfil = perfilService.listarPerfisDoUsuario().stream().findFirst();

        if (meuPerfil.isEmpty()) {
            redirectAttributes.addFlashAttribute("msgErro", "Você precisa criar um perfil antes de visualizar ou interagir com o match.");
            return "redirect:/perfis";
        }

        // Usa os métodos refatorados
        List<Perfil> perfisNaoReagidos = perfilService.obterPerfisNaoReagidos(meuPerfil.get());
        List<PerfilMatchDTO> perfisCompativeis = perfilService.calcularCompatibilidade(meuPerfil.get(), perfisNaoReagidos);

        model.addAttribute("perfilAtual", meuPerfil.get());
        model.addAttribute("perfisCompativeis", perfisCompativeis);
        return "compativeis";
    }


    //O usuario Da Match Sim
    @PostMapping("/perfis/{perfilOrigemId}/match/{perfilAlvoId}/sim")
    public ResponseEntity<Void> darMatchSim(
            @PathVariable("perfilOrigemId") String perfilOrigemId,
            @PathVariable("perfilAlvoId") String perfilAlvoId) {
        decisaoMatchService.salvarDecisao(perfilOrigemId, perfilAlvoId, true);
        return ResponseEntity.ok().build();
    }

    //O usuario Da Match Sim
    @PostMapping("/perfis/{perfilOrigemId}/match/{perfilAlvoId}/nao")
    public ResponseEntity<Void> darMatchNao(
            @PathVariable("perfilOrigemId") String perfilOrigemId,
            @PathVariable("perfilAlvoId") String perfilAlvoId) {
        decisaoMatchService.salvarDecisao(perfilOrigemId, perfilAlvoId, false);
        return ResponseEntity.ok().build();
    }

    //Lista Matches Reciprocos
    @GetMapping("/perfis/matches-reciprocos")
    public String listarMatchesRecebidos(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Optional<Perfil> meuPerfil = perfilService.buscarPerfilDoUsuarioLogado(principal.getName());

        if (meuPerfil.isEmpty()) {
            redirectAttributes.addFlashAttribute("msgErro", "Você precisa criar um perfil antes de visualizar ou interagir com os meus matches.");
            return "redirect:/perfis";
        }

        List<DecisaoMatch> matchesComigo = decisaoMatchService.listarMatchesComigo(meuPerfil.get().getId());

        Set<String> ids = matchesComigo.stream()
                .map(DecisaoMatch::getPerfilOrigemId)
                .collect(Collectors.toSet());

        List<Perfil> perfis = perfilService.buscarPerfisPorIds(ids);
        model.addAttribute("meuPerfil", meuPerfil.get());
        model.addAttribute("perfisQueDeramMatch", perfis);
        return "matches-reciprocos";
    }

    //Ver Curtidas Recebidas
    @GetMapping("/perfis/curtidas-recebidas")
    public String verCurtidasRecebidas(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Optional<Perfil> meuPerfil = perfilService.buscarPerfilDoUsuarioLogado(principal.getName());

        if (meuPerfil.isEmpty()) {
            redirectAttributes.addFlashAttribute("msgErro", "Você precisa criar um perfil antes de visualizar ou interagir com as curtidas recebidas.");
            return "redirect:/perfis";
        }

        List<Perfil> perfisQueMeCurtiram = decisaoMatchService.listarQuemMeCurtiuSemResposta(meuPerfil.get().getId());

        model.addAttribute("perfis", perfisQueMeCurtiram);
        model.addAttribute("perfilAtual", meuPerfil.get());
        return "curtidas-recebidas";
    }

}