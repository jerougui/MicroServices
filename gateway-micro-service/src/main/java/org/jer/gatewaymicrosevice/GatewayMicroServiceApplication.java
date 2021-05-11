package org.jer.gatewaymicrosevice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
@EnableHystrix
public class GatewayMicroServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayMicroServiceApplication.class, args);
	}
	@Bean
	DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc,
														DiscoveryLocatorProperties dlp) {
		return new DiscoveryClientRouteDefinitionLocator(rdc, dlp);
	}

	// url to use : localhost:8888/PRODUCT-SERVICE/products
	// instead of localhost:8888/products
	// url to use : localhost:8888/CUSTOMER-SERVICE/products
	// instead of localhost:8888/customer
	@Bean
	RouteLocator staticsRoutes(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()//
		//.route(r -> r.path("/customers/**").uri("lb://CUSTOMER-SERVICE"))
		//.route(r -> r.path("/products/**").uri("lb://PRODUCT-SERVICE"))//
		.route(r -> r.path("/publicCountries/**")
				.filters(f -> f
						.addRequestHeader("x-rapidapi-key", "e0b8613ed8msh606a2cd410643e3p1be96djsn5ec16ff61be7")
						.addRequestHeader("x-rapidapi-host", "referential.p.rapidapi.com")
						.rewritePath("/publicCountries/(?<segment>.*)", "/${segment}")
						//.hystrix(h -> h.setName("countries").setFallbackUri("forward:/defaultCountries"))
				)
				.uri("https://referential.p.rapidapi.com/v1/country?fields=currency%2Ccurrency_num_code%2Ccurrency_code%2Ccontinent_code%2Ccurrency%2Ciso_a3%2Cdial_code"))

				.route(r -> r.path("/muslim/**")
						.filters(f -> f
								.addRequestHeader("x-rapidapi-key", "e0b8613ed8msh606a2cd410643e3p1be96djsn5ec16ff61be7")
								.addRequestHeader("x-rapidapi-host", "muslimsalat.p.rapidapi.com")
								.rewritePath("/muslim/(?<segment>.*)", "/${segment}")
						)
						// endpoint exemple call http://desktop-bsaolul:8888/muslim/Nantes/1.json
						.uri("https://muslimsalat.p.rapidapi.com/london.json"))
				.build();
	}

}

@RestController
class CircuitBreakerController{
	@GetMapping("defaultCountries")
	public Map<String,String> countries() {
		Map<String, String> data = new HashMap<>();
		data.put("message", "default countries");
		data.put("countries", "Maroc, Algérie");
		return  data;
	}
}