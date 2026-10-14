package kz.iitu.springlab;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@ConfigurationPropertiesScan
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class SpringLab01Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringLab01Application.class, args);
	}

}
