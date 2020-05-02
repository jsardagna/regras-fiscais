package com.contaazul.fiscal.enttiy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.contaazul.fiscal.enttiy.mva.Item;
import com.contaazul.fiscal.service.mva.CestService;
import com.contaazul.fiscal.utils.FiscalCrawlerUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Cest {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@EqualsAndHashCode.Exclude
	private Long id;

	@Column(unique = true)
	String codigo;

	public static void addCEST(CestService cestService, Item item, String value) {
		value = FiscalCrawlerUtils.corrigeTexto(value);
		Cest cest = cestService.findByCodigo(value);
		if (cest == null) {
			cest = new Cest();
			cest.setCodigo(value);
			cestService.save(cest);
		}
		item.setCest(cest);
	}

}
