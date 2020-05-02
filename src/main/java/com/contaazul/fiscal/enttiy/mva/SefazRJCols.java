package com.contaazul.fiscal.enttiy.mva;

public enum SefazRJCols {

	NUM("Subitem.*"), //
	DESCRICAO("Descrição.*"), //
	NCM("NCM/SH.*"), //
	CEST("CEST.*"), //
	MVAINTERNA("MVA Original.*"), //
	MVA12("(?=Alíquota)(?=.*?interestadual)(?=.*?12).*"), //
	MVA4("(?=Alíquota)(?=.*?interestadual)(?=.*?4).*");//

	public String regex;

	SefazRJCols(String regex) {
		this.regex = regex;
	}
}
