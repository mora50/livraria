package com.cesar.livraria;

import org.springframework.boot.SpringApplication;

public class TestLivrariaApplication {

	public static void main(String[] args) {
		SpringApplication.from(LivrariaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
