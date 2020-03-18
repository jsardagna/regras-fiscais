package com.contaazul.fiscal;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Teste {

	public static void main(String[] args) {
		System.out.println("-".matches(".*[a-zA-Z0-9].*"));
		System.out.println("1.0L".matches(".*([^0-9,]).*"));
		System.out.println("--fdf1,0L".replaceAll("[^0-9,]+", ""));
	}

}
