package com.contaazul.fiscal.service.mva;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.contaazul.fiscal.enttiy.mva.Lei;
import com.contaazul.fiscal.utils.Estados;

public interface LeiService extends CrudRepository<Lei, Long> {

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Lei c SET c.ultimaVersao = (select true " //
			+ "                                 from Lei b " //
			+ "                                where b.id = c.id  " //
			+ "                                  and c.efeito = (select max(a.efeito) " //
			+ "                                                    from Lei a " //
			+ "                                                   where a.estado = c.estado))") //
	void updateUltimaVersao();

	Lei findByArquivo(String arquivo);

	Lei findByArquivoAndEfeito(String arquivo, LocalDate efeito);

	List<Lei> findByEstadoAndEfeitoGreaterThanEqual(Estados estado, LocalDate efeito);
}
