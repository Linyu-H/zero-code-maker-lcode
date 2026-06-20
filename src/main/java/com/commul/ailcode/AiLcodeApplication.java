package com.commul.ailcode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.commul.ailcode.mapper")
public class AiLcodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiLcodeApplication.class, args);
	}

}
