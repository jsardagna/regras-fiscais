package com.contaazul.fiscal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.contaazul.fiscal.service.mva.ConfazCrawlerService;
import com.contaazul.fiscal.service.mva.SefazRJCrawlerService;
import com.contaazul.fiscal.service.mva.SefazRSCrawlerService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/fiscal/mva")
public class MVAUpdate {

	@Autowired
	ConfazCrawlerService confaz;

	@Autowired
	SefazRSCrawlerService sefazrs;

	@Autowired
	SefazRJCrawlerService sefazjs;

	@RequestMapping(value = "/regra/update", method = RequestMethod.GET)
	public String regras() {
		confaz.updateRules();
		sefazrs.updateRules();
		sefazjs.updateRules();
		return "";
	}

	@RequestMapping(value = "/arquivo/update", method = RequestMethod.GET)
	public String update() {
		confaz.updateFile();
		sefazrs.updateFile();
		sefazjs.updateFile();
		return "";
	}

}
