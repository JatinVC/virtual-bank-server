package com.jatinc.ebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class VirtualBankServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VirtualBankServerApplication.class, args);
    }

}
