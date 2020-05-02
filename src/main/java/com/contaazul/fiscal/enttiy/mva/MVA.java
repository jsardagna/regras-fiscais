package com.contaazul.fiscal.enttiy.mva;

import java.math.BigDecimal;
import java.text.ParseException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.contaazul.fiscal.utils.FiscalCrawlerUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MVA {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	Item item;

	String tipo;

	String descricao;

	BigDecimal valor;

	public static void addMVA(Item item, String value, String descricao, String tipo) throws ParseException {
		value = FiscalCrawlerUtils.corrigeTexto(value);
		MVA mva = new MVA();
		mva.setDescricao(descricao);
		mva.setTipo(tipo);
		if (FiscalCrawlerUtils.isNumericc(value)) {
			mva.setValor(FiscalCrawlerUtils.corrigeValor(value));
		} else {
			mva.setDescricao(value);
		}
		item.addMVA(mva);
		Especificacao especificacao = new Especificacao();
		especificacao.setDescricao(descricao);
		especificacao.setTipo(tipo);
		especificacao.setEspecificacao(descricao);
		item.addEspecificacao(especificacao);
	}

}
