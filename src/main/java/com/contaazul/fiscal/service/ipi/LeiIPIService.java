package com.contaazul.fiscal.service.ipi;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.contaazul.fiscal.enttiy.ipi.LeiIPI;

public interface LeiIPIService extends CrudRepository<LeiIPI, Long> {

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE LeiIPI c SET c.ultimaVersao = (select true " + //
			"                                 from LeiIPI b where b.id = c.id  " //
			+ "  					  	                     and c.atualizacao = (select max(a.atualizacao) from LeiIPI a ))")
	void updateUltimaVersao();

	LeiIPI findByAtualizacao(LocalDate atualizacao);
}
