package com.contaazul.fiscal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.contaazul.fiscal.service.ipi.IPICrawlerService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/fiscal/ipi")
public class IPIUpdate {

	@Autowired
	IPICrawlerService crawler;

	@RequestMapping(value = "/regra/update", method = RequestMethod.GET)
	public String update() {
		return crawler.updateFileAndRules();

	}

	@RequestMapping(value = "/arquivo/update", method = RequestMethod.GET)
	public String updateArquivoIPI() {
		crawler.updateArquivoIPI();
		return "Concluido";
	}
}
