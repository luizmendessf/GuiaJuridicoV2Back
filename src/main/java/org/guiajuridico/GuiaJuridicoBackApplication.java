package org.guiajuridico;

import org.guiajuridico.config.AppProperties;
import org.guiajuridico.config.BrevoProperties;
import org.guiajuridico.config.NewsletterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ AppProperties.class, BrevoProperties.class, NewsletterProperties.class })
public class GuiaJuridicoBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuiaJuridicoBackApplication.class, args);
	}

}
