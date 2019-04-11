package com.rbkmoney.reporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * Created by tolkonepiu on 10/07/2017.
 */

@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.reporter"})
public class ReporterApplication {

    public static void main(String... args) {
        SpringApplication.run(ReporterApplication.class, args);
    }
}
