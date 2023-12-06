package com.pichellitech.minhasfinancas.model.repository;

import com.pichellitech.minhasfinancas.model.entity.Lancamento;
import com.pichellitech.minhasfinancas.model.enums.StatusLancamento;
import com.pichellitech.minhasfinancas.model.enums.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LancamentoRepository extends JpaRepository<Lancamento,Long> {
    @Query("SELECT SUM(l.valor) FROM Lancamento l WHERE l.usuario.id = :idUsuario AND l.tipo = :tipo AND l.status = :status")
    BigDecimal obterSaldoPorTipoLancamentoEUsuarioEStatus(
            @Param("idUsuario") Long idUsuario,
            @Param("tipo") TipoLancamento tipo,
            @Param("status") StatusLancamento status);
}
