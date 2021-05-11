package org.jer.productsevice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity @Data @AllArgsConstructor @NoArgsConstructor
class Product {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	private String name;
	private double price;

}

@Projection( name = "p1", types = {Product.class})
interface ProductProjection {
	Long getId();
	String getPrice();
}



@RepositoryRestResource
interface ProductRepository extends JpaRepository<Product, Long> {

}

@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner start(ProductRepository productRepository, RepositoryRestConfiguration restConfiguration) {
		return args -> {
			restConfiguration.exposeIdsFor(Product.class);
			productRepository.save(new Product(null, "Azus", 1320.0));
			productRepository.save(new Product(null, "Dell", 499.0 ));
			productRepository.save(new Product(null, "hp", 99.99 ));
			productRepository.findAll().forEach(System.out::println);
		};
	}

}
