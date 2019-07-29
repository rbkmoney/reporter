package com.rbkmoney.reporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.reporter"})
public class ReporterApplication {

    public static void main(String... args) {
        SpringApplication.run(ReporterApplication.class, args);
    }
}
