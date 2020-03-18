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
public class Segmento {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	Lei lei;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "segmento_id")
	List<Item> items = new ArrayList<Item>();
	
	public void addItem(Item item){
		items.add(item);
		item.setSegmento(this);
	}

	String versao;

	LocalDate efeito;

	String descricaoSegmento;
	
	String nome;

	@Column(length = 4000)
	String observacoes;
}
