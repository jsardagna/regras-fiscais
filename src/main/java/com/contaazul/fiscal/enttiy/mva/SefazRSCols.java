package com.contaazul.fiscal.enttiy.mva;

public enum SefazRSCols {

	NUM("ITEM|NÚMERO"), //
	DESCRICAO("MERCADORIAS"), //
	NCM("CLASSIFICAÇÃO NA NBM/SH-NCM"), //
	CEST("(?=CÓDIGO)(?=.*? - CEST).*"), //
	MVAINTERNA("(?=OPERAÇÃO)(?=.*?INTERNA).*"), //
	MVA12("Coluna I|SUJEITA À ALÍQUOTA DE 12%"), //
	MVA4("Coluna II|SUJEITA À ALÍQUOTA DE 4%");//

	public String regex;

	SefazRSCols(String regex) {
		this.regex = regex;
	}
}
