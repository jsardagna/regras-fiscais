package com.contaazul.fiscal.enttiy.ipi;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.StringUtils;

import com.contaazul.fiscal.enttiy.NCM;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IPI {

	public static final DateTimeFormatter formatterD = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	public static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	NCM ncm;

	@ManyToOne
	LeiIPI leiipi;

	@Column(length = 4000)
	String excessao;

	BigDecimal valor;

	public void corrigeValor(String aliquota) throws ParseException {
		if (StringUtils.isNumeric(aliquota)) {
			this.setValor(BigDecimal.valueOf(numberFormat.parse(aliquota).doubleValue()));
		} else {
			this.setValor(BigDecimal.ZERO);
			this.setExcessao(aliquota);
		}
	}

}
