package com.contaazul.fiscal.enttiy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class NCM {

	@Id
	String codigo;

	@Column(length = 4000)
	String descricao;

}
