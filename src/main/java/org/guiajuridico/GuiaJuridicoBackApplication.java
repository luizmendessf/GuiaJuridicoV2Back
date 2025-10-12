package org.guiajuridico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD

@SpringBootApplication
=======
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
>>>>>>> 7b0b384 (V2Back: add main class, global exception handler, oportunidades controller, application.properties; upgrade JJWT; enable env-configurable port/JWT secret)
public class GuiaJuridicoBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuiaJuridicoBackApplication.class, args);
    }
}