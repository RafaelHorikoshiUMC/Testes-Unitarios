package com.pfc.thindesk.service;

import com.pfc.thindesk.MatchDTO;
import com.pfc.thindesk.entity.DecisaoMatch;
import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.repository.DecisaoMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DecisaoMatchServiceTest {

    @Mock
    private DecisaoMatchRepository repository;

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private DecisaoMatchService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // inicializa os mocks
    }

    @Test
    void testSalvarDecisao() {
        service.salvarDecisao("1", "2", true);
        verify(repository, times(1)).save(any(DecisaoMatch.class));
    }
    @Test
    void testListarTodasDecisaoMatch() {
        when(repository.findAll()).thenReturn(List.of(new DecisaoMatch()));
        List<DecisaoMatch> result = service.listarTodasDecisaoMatch();

        assertEquals(1, result.size());
    }

    @Test
    void testVerificarMatchQuandoAmbosDeramLike() {
        String idA = "1";
        String idB = "2";

        Perfil perfilA = new Perfil();
        perfilA.setId(idA);
        perfilA.setApelido("João");

        Perfil perfilB = new Perfil();
        perfilB.setId(idB);
        perfilB.setApelido("Maria");

        when(perfilService.buscarPerfilPorIdSemRestricao(idA)).thenReturn(Optional.of(perfilA));
        when(perfilService.buscarPerfilPorIdSemRestricao(idB)).thenReturn(Optional.of(perfilB));

        when(repository.existsByPerfilOrigemIdAndPerfilAlvoIdAndDeuMatch(idA, idB, true)).thenReturn(true);
        when(repository.existsByPerfilOrigemIdAndPerfilAlvoIdAndDeuMatch(idB, idA, true)).thenReturn(true);

        MatchDTO match = service.verificarMatch(idA, idB);

        assertTrue(match.isHouveMatch());
        assertEquals("João", match.getApelidoPerfilA());
        assertEquals("Maria", match.getApelidoPerfilB());
    }

    @Test
    void testListarMatchesComigo() {
        String meuId = "meu";

        DecisaoMatch match = new DecisaoMatch();
        match.setPerfilOrigemId("outro");

        when(repository.findByPerfilAlvoIdAndDeuMatch(meuId, true)).thenReturn(List.of(match));
        when(repository.existsByPerfilOrigemIdAndPerfilAlvoIdAndDeuMatch(meuId, "outro", true)).thenReturn(true);

        List<DecisaoMatch> result = service.listarMatchesComigo(meuId);

        assertEquals(1, result.size());
    }

    @Test
    void testListarQuemMeCurtiuSemResposta() {
        String meuId = "meu";
        String outroId = "outro";

        DecisaoMatch match = new DecisaoMatch();
        match.setPerfilOrigemId(outroId);

        Perfil perfilOutro = new Perfil();
        perfilOutro.setId(outroId);
        perfilOutro.setApelido("Fulano");

        when(repository.findByPerfilAlvoIdAndDeuMatch(meuId, true)).thenReturn(List.of(match));
        when(repository.existsByPerfilOrigemIdAndPerfilAlvoId(meuId, outroId)).thenReturn(false);
        when(perfilService.buscarPerfilPorIdSemRestricao(outroId)).thenReturn(Optional.of(perfilOutro));

        List<Perfil> result = service.listarQuemMeCurtiuSemResposta(meuId);

        assertEquals(1, result.size());
        assertEquals("Fulano", result.get(0).getApelido());
    }
}
