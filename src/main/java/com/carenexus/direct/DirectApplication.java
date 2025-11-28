package com.carenexus.direct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.carenexus.direct",
        "com.carenexus.auth"
})
@ComponentScan(basePackages = {
        "com.carenexus.direct",
        "com.carenexus.auth"
},
excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = com.carenexus.auth.exception.GlobalExceptionHandler.class)
})
@EnableJpaRepositories(basePackages = {
        "com.carenexus.direct.repository",
        "com.carenexus.auth.repository"
})
@EntityScan(basePackages = {
        "com.carenexus.direct.model",
        "com.carenexus.auth.model"
})
public class DirectApplication {

	public static void main(String[] args) {
		SpringApplication.run(DirectApplication.class, args);
	}

}
