package com.contaazul.fiscal.service;

import org.springframework.data.repository.CrudRepository;

import com.contaazul.fiscal.enttiy.NCM;

public interface NCMService extends CrudRepository<NCM, Long> {

	public NCM findByCodigo(String codigo);

}
