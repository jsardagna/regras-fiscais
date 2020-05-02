package com.contaazul.fiscal.service.mva;

import org.springframework.data.repository.CrudRepository;

import com.contaazul.fiscal.enttiy.Cest;

public interface CestService extends CrudRepository<Cest, Long> {

	public Cest findByCodigo(String codigo);

}
