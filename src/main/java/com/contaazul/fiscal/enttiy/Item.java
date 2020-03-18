package com.contaazul.fiscal.enttiy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	Long ordem;

	@Column(length = 1000)
	String descricao;

	String opInterna;

	@ManyToOne
	Segmento segmento;

	@ManyToOne(cascade = CascadeType.PERSIST)
	Cest cest;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	List<Acordo> acordoEstados = new ArrayList<Acordo>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	List<Aliquota> aliquotas = new ArrayList<Aliquota>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	List<MVA> mvas = new ArrayList<MVA>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	List<PFC> pfcs = new ArrayList<PFC>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	List<Especificacao> especificacoes = new ArrayList<Especificacao>();

	public void addAliquota(Aliquota aliquota) {
		aliquotas.add(aliquota);
		aliquota.setItem(this);
	}

	public void addPFC(PFC a) {
		pfcs.add(a);
		a.setItem(this);
	}

	public void addMVA(MVA e) {
		mvas.add(e);
		e.setItem(this);
	}

	public void addEspecificacao(Especificacao e) {
		especificacoes.add(e);
		e.setItem(this);
	}

	public void addAcordo(Acordo acordo) {
		acordoEstados.add(acordo);
		acordo.setItem(this);
	}

	LocalDate criacao;
}
