package com.contaazul.fiscal.enttiy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import com.contaazul.fiscal.utils.Estados;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Lei {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "lei_id")
	List<Segmento> segmentos = new ArrayList<Segmento>();

	public void addSegmento(Segmento segmento) {
		segmentos.add(segmento);
		segmento.setLei(this);
	}

	@Enumerated(EnumType.STRING)
	Estados estado;

	LocalDate criacao;
	
	LocalDate efeito;
	
	String versao;
	
	Boolean ultimaVersao;
	
	@Column(length = 1000)
	String arquivo;
}
