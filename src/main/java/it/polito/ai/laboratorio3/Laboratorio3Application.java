package it.polito.ai.laboratorio3;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Laboratorio3Application {

    public static void main(String[] args) {
        SpringApplication.run(Laboratorio3Application.class, args);
    }

    @Bean
    ModelMapper modelMapper(){
        return new ModelMapper();
    }


}
