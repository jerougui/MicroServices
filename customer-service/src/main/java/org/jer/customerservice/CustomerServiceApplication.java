package org.jer.customerservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity @Data @AllArgsConstructor @NoArgsConstructor
class Customer {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	private String name;
	private String email;
	private String tel;

}

@Projection( name = "p1", types = {Customer.class})
interface CustomerProjection {
	Long getId();
	String getName();
}

@Projection( name = "p2", types = {Customer.class})
interface CustomerProjection2 {
	Long getEmail();
	String getTel();
}


@RepositoryRestResource
interface CustomerRepository extends JpaRepository<Customer, Long> {

}

@SpringBootApplication
public class CustomerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner start(CustomerRepository customerRepository, RepositoryRestConfiguration restConfiguration) {
		return args -> {
			restConfiguration.exposeIdsFor(Customer.class);
			customerRepository.save(new Customer(null, "alice", "a.011.lice@gmail.com", "06.60.55.88.99" ));
			customerRepository.save(new Customer(null, "bob", "bob05@gmail.com", "07.88.55.88.99" ));
			customerRepository.save(new Customer(null, "kadour", "kadour@gmail.com", "05.10.55.88.99" ));
			customerRepository.findAll().forEach(System.out::println);
		};
	}

}
