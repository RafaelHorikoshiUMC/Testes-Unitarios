package com.pfc.thindesk.service;

import com.pfc.thindesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //Carrega os dados do usuário com base no nome de usuário. (Spring Security)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca no repositório o usuário pelo email fornecido
        // O metodo findByEmail retorna um Optional<UserDetails>
        // Se o usuário não for encontrado, lança uma exceção UsernameNotFoundException com mensagem personalizada
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

}

