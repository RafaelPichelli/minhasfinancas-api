package com.pichellitech.minhasfinancas.service;

import com.pichellitech.minhasfinancas.exception.ErroAutenticacao;
import com.pichellitech.minhasfinancas.exception.RegraNegocioException;
import com.pichellitech.minhasfinancas.model.entity.Usuario;
import com.pichellitech.minhasfinancas.model.repository.UsuarioRepository;
import com.pichellitech.minhasfinancas.service.impl.UsuarioServiceImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
    @SpyBean
    UsuarioServiceImpl service;
    @MockBean
    UsuarioRepository repository;

    @Test
    public void deveValidarEmail(){
        Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

        assertDoesNotThrow(() -> {
           service.validarEmail("email@email.com");
        });
    }

    @Test
    public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado(){
        Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

        assertThrows(RegraNegocioException.class,() -> {
            service.validarEmail("email@email.com");
        });
    }

    @Test
    public void deveAutenticarUmUsuarioComSucesso(){
        String email = "email@email.com";
        String senha = "senha";

        Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
        Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

        assertDoesNotThrow(()->{
            Usuario result = service.autenticar(email,senha);
            Assertions.assertThat(result).isNotNull();
        });
    }

    @Test
    public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComEmailInformado(){
        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(ErroAutenticacao.class,() -> {
           service.autenticar("email@email.com","senha");
        });
    }

    @Test
    public void deveLancarErroQuandoSenhaNaoBaterNaAutenticacaoDeUsuario(){
        String senha = "senha";
        Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

        assertThrows(ErroAutenticacao.class,() -> {
            service.autenticar("email@email.com", "senhaErrada");
        });
    }

    @Test
    public void deveSalvarUmUsuario(){
        Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
        Usuario usuario = Usuario
                .builder()
                .id(1l)
                .nome("nome")
                .email("email@email.com")
                .senha("senha")
                .build();

        Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

        assertDoesNotThrow(()-> {
            Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
            Assertions.assertThat(usuarioSalvo).isNotNull();
            Assertions.assertThat(usuarioSalvo.getId()).isEqualTo(1L);
            Assertions.assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
            Assertions.assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
            Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
        });
    }

    @Test
    public void naoDeveSalvarUsuarioComEmailJaCadastrado(){
        String email = "email@email.com";
        Usuario usuario = Usuario.builder().email(email).build();
        Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

        assertThrows(RegraNegocioException.class,() -> {
            service.salvarUsuario(usuario);
            Mockito.verify(repository, Mockito.never()).save(usuario);

        });

    }
}
