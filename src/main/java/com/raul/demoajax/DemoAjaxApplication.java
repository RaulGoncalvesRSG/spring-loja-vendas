package com.raul.demoajax;

import org.directwebremoting.spring.DwrSpringServlet;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

//Importa o arq de configuração xml criado: dwr-spring.xml. Qnd trabalha com arq xml, precisa da anotação
@ImportResource(locations = "classpath:dwr-spring.xml")
@SpringBootApplication
public class DemoAjaxApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoAjaxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	}
	
	@Bean				//Transforma o método em bean gerenciado pelo spring
	public ServletRegistrationBean<DwrSpringServlet> dwrSpringServlet() {
		DwrSpringServlet dwrServlet = new DwrSpringServlet();
		
		ServletRegistrationBean<DwrSpringServlet> registrationBean = 
				new ServletRegistrationBean<>(dwrServlet, "/dwr/*");
		
		//Habilita o recurso de debug da dwr, tem informação no log da aplicação sobre qualquer erro
		registrationBean.addInitParameter("debug", "true");
		//Habilita o servidor para trabalhar com ajax reverso dwr
		registrationBean.addInitParameter("activeReverseAjaxEnabled", "true");
		
		return registrationBean;
	}
}
