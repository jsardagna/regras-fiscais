package com.contaazul.fiscal.enttiy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LeiIPI {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "leiipi_id")
	List<IPI> itens = new ArrayList<IPI>();

	public void addItem(IPI ipi) {
		itens.add(ipi);
		ipi.setLeiipi(this);
	}

	LocalDate criacao;

	LocalDate atualizacao;

	Boolean ultimaVersao;

}
