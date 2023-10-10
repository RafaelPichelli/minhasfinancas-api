package com.pichellitech.minhasfinancas.service;

import com.pichellitech.minhasfinancas.exception.RegraNegocioException;
import com.pichellitech.minhasfinancas.model.entity.Lancamento;
import com.pichellitech.minhasfinancas.model.entity.Usuario;
import com.pichellitech.minhasfinancas.model.enums.StatusLancamento;
import com.pichellitech.minhasfinancas.model.repository.LancamentoRepository;
import com.pichellitech.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.pichellitech.minhasfinancas.service.impl.LancamentoServiceImp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

    @SpyBean
    LancamentoServiceImp serviceImp;

    @MockBean
    LancamentoRepository repository;

    @Test
    public void deveSalvarUmLancamento(){
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
        Mockito.doNothing().when(serviceImp).validar(lancamentoASalvar);

        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1l);
        lancamentoSalvo.setStatus(StatusLancamento.EFETIVADO);
        Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);

        Lancamento lancamento = serviceImp.salvar(lancamentoASalvar);

        Assertions.assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
        Assertions.assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.EFETIVADO);

    }

    @Test
    public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao(){
        Lancamento lancamentoAsalvar = LancamentoRepositoryTest.criarLancamento();
        Mockito.doThrow(RegraNegocioException.class).when(serviceImp).validar(lancamentoAsalvar);

        Assertions.catchThrowableOfType(() -> serviceImp.salvar(lancamentoAsalvar), RegraNegocioException.class);

        Mockito.verify(repository, Mockito.never()).save(lancamentoAsalvar);
    }

    @Test
    public void deveAtualizarUmLancamento(){
        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1l);
        lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);

        Mockito.doNothing().when(serviceImp).validar(lancamentoSalvo);

        Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);

        serviceImp.atualizar(lancamentoSalvo);

        Mockito.verify(repository,Mockito.times(1)).save(lancamentoSalvo);
    }

    @Test
    public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo(){
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        Assertions.catchThrowableOfType(() -> serviceImp.atualizar(lancamento), NullPointerException.class);
        Mockito.verify(repository, Mockito.never()).save(lancamento);
    }

    @Test
    public void deveDeletarUmLancamento(){
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1l);

        serviceImp.deletar(lancamento);

        Mockito.verify(repository).delete(lancamento);
    }

    @Test
    public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo(){
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        Assertions.catchThrowableOfType(() -> serviceImp.deletar(lancamento), NullPointerException.class);

        Mockito.verify(repository, Mockito.never()).delete(lancamento);
    }

    @Test
    public void deveFiltrarLancamentos(){
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1l);

        List<Lancamento> lista = Arrays.asList(lancamento);
        Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lista);

        List<Lancamento> resultado = serviceImp.buscar(lancamento);

        Assertions.assertThat(resultado)
                .isNotEmpty()
                .hasSize(1)
                .contains(lancamento);
    }

    @Test
    public void deveAtualizarStatus(){
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1l);
        lancamento.setStatus(StatusLancamento.PENDENTE);

        StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
        Mockito.doReturn(lancamento).when(serviceImp).atualizar(lancamento);

        serviceImp.atualizarStatus(lancamento,novoStatus);

        Assertions.assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
        Mockito.verify(serviceImp).atualizar(lancamento);

    }

    @Test
    public void deveObterUmLancamentoPorId(){
        Long id = 1l;

        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));

        Optional<Lancamento> resultado = serviceImp.obterPorId(id);

        Assertions.assertThat(resultado.isPresent()).isTrue();
    }

    @Test
    public void deveRetornarVazioQuandoLancamentoNaoExiste(){
        Long id = 1l;

        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Lancamento> resultado = serviceImp.obterPorId(id);

        Assertions.assertThat(resultado.isPresent()).isFalse();
    }

    @Test
    public void deveLancarErrosAoValidarUmLancamento() {
        Lancamento lancamento = new Lancamento();

        Throwable erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida.");

        lancamento.setDescricao("");

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida.");

        lancamento.setDescricao("Salario");

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido.");

        lancamento.setMes(0);

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido.");

        lancamento.setMes(13);

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido.");

        lancamento.setMes(1);

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido");

        lancamento.setAno(202);

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido");

        lancamento.setAno(2020);

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");

        lancamento.setUsuario(new Usuario());

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");

        lancamento.getUsuario().setId(1l);

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido.");

        lancamento.setValor(BigDecimal.ZERO);

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido.");

        lancamento.setValor(BigDecimal.ONE);

        erro = Assertions.catchThrowable(() -> serviceImp.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um tipo de lançamento.");
    }
}
