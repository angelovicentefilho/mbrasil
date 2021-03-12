package br.com.mbrasil.hellow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.mbrasil.scheduler.annotation.EnableMbScheduling;

@SpringBootApplication
@EnableMbScheduling
public class MbHelloworldApplication {

	public static void main(String[] args) {
		SpringApplication.run(MbHelloworldApplication.class, args);
	}

}
