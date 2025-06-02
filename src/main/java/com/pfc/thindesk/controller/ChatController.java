package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Grupo;
import com.pfc.thindesk.entity.Mensagem;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.GrupoRepository;
import com.pfc.thindesk.repository.MensagemRepository;
import com.pfc.thindesk.repository.PerfilRepository;
import com.pfc.thindesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    //Abre o Chat
    @GetMapping("/{destinatarioId}")
    public String abrirChat(@PathVariable String destinatarioId, Model model, Principal principal) {
        String email = principal.getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Perfil perfilLogado = perfilRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));



        List<Mensagem> mensagens = mensagemRepository
                .findByRemetenteIdAndDestinatarioIdOrRemetenteIdAndDestinatarioId(
                        perfilLogado.getId(), destinatarioId,
                        destinatarioId, perfilLogado.getId()
                );

        Perfil destinatario = perfilRepository.findById(destinatarioId).orElse(null);

        model.addAttribute("mensagens", mensagens);
        model.addAttribute("perfilLogado", perfilLogado);
        model.addAttribute("destinatario", destinatario);
        model.addAttribute("novaMensagem", new Mensagem());

        return "chat";
    }

    //Envia a Mensagem
    @PostMapping("/enviar")
    public String enviarMensagem(@ModelAttribute Mensagem novaMensagem, Principal principal) {
        String email = principal.getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Perfil perfilLogado = perfilRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        novaMensagem.setRemetenteId(perfilLogado.getId());
        novaMensagem.setDataHora(LocalDateTime.now());

        mensagemRepository.save(novaMensagem);

        return "redirect:/chat/" + novaMensagem.getDestinatarioId();
    }

    //Abre o Chat do Grupo
    @GetMapping("/grupo/{grupoId}")
    public String abrirChatGrupo(@PathVariable String grupoId, Model model, Principal principal) {
        String email = principal.getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Perfil perfilLogado = perfilRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        if (!grupo.getParticipantes().contains(perfilLogado) && !grupo.getPerfilCriador().getId().equals(perfilLogado.getId())) {
            throw new RuntimeException("Você não faz parte deste grupo.");
        }

        List<Mensagem> mensagens = mensagemRepository.findByGrupoIdOrderByDataHora(grupoId);

        model.addAttribute("grupo", grupo);
        model.addAttribute("perfilLogado", perfilLogado);
        model.addAttribute("mensagens", mensagens);
        model.addAttribute("novaMensagem", new Mensagem());

        return "chat-grupo";
    }

    //Envia Mensagem no Grupo
    @PostMapping("/grupo/enviar")
    public String enviarMensagemGrupo(@RequestParam String grupoId,
                                      @RequestParam String conteudo,
                                      Principal principal) {
        String email = principal.getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Perfil perfilLogado = perfilRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        if (!grupo.getParticipantes().contains(perfilLogado) && !grupo.getPerfilCriador().getId().equals(perfilLogado.getId())) {
            throw new RuntimeException("Você não faz parte deste grupo.");
        }

        Mensagem mensagem = new Mensagem();
        mensagem.setGrupoId(grupoId);
        mensagem.setRemetenteId(perfilLogado.getId());
        mensagem.setConteudo(conteudo);
        mensagem.setDataHora(LocalDateTime.now());

        mensagemRepository.save(mensagem);

        return "redirect:/chat/grupo/" + grupoId;
    }


}

