package org.jer.billservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Data
class Customer {
	private Long id; private String name; private String email;
}

// enable interaction between WS to get and also to post information
@FeignClient(name = "CUSTOMER-SERVICE")
interface CustomerService {
	@GetMapping("/customers/{id}")
	public Customer findCustomerById(@PathVariable(name="id") Long id);
}

@Data
class Product {
	private Long id; private String name; private double price;
}

@FeignClient(name = "PRODUCT-SERVICE")
interface InventoryService {
	@GetMapping("/products/{id}")
	public Product findProductById(@PathVariable(name="id") Long id);

	@GetMapping("/products")
	public PagedModel<Product> findAllProducts();
}

@Entity @Data @NoArgsConstructor @AllArgsConstructor
class Bill{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String category;
	@Transient  private Customer customer;  // not persisted
	private Date billingDate;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Long customerID;
	@OneToMany(mappedBy = "bill")
	private Collection<ProductItem> productItems;
}
@Entity @Data @NoArgsConstructor @AllArgsConstructor
class ProductItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String productName;
	@Transient  private Product product; // not persisted
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Long productID;
	private double price;
	private double quantity;
	@ManyToOne
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Bill bill;
}

@CrossOrigin(origins ="*")
@RepositoryRestResource
interface BillRepository extends JpaRepository<Bill, Long> {

}
@RepositoryRestResource
interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
}

@Projection(name = "mybill", types={Bill.class})
interface fullProjection {
	public Long getId();
	public Date getBillingDate();
	public Long  getCustomerID();
	public Collection<ProductItem> getProductItems();
}

@SpringBootApplication @EnableFeignClients
public class BillServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner start(BillRepository billRepository,
							ProductItemRepository productItemRepository,
							CustomerService customerService,
							InventoryService inventoryService,
							RepositoryRestConfiguration restConfiguration) {
		return args -> {
			// to expose ids in Json, to enable request, retrieve and browse associated object
			restConfiguration.exposeIdsFor(Bill.class);

			Customer c1 = customerService.findCustomerById(1L);
			Customer c2 = customerService.findCustomerById(2L);
			Customer c3 = customerService.findCustomerById(3L);

			System.out.println("==========================");
			System.out.println("Id: " + c1.getId());
			System.out.println("name: " + c1.getName());
			System.out.println("Email: " + c1.getEmail());
			System.out.println("==========================");

			Product p1  = inventoryService.findProductById(1L);
			System.out.println("==========================");
			System.out.println("Product Id: " + p1.getId());
			System.out.println("name: " + p1.getName());
			System.out.println("Price: " + p1.getPrice());
			System.out.println("==========================");

			Product p2  = inventoryService.findProductById(2L);
			Product p3  = inventoryService.findProductById(3L);

			System.out.println("================ All Product : Start =================");
			inventoryService.findAllProducts().getContent().forEach(
					p-> System.out.println(p.getId() + " | " + p.getName() + " | " + p.getPrice())
			);
			System.out.println("================ All Product : End ====================");

			Bill bill1 = billRepository.save(new Bill(null,  "Fruits et légumes", c1, new Date() , c1.getId(), null));
			Bill bill2 = billRepository.save(new Bill(null, "Produits laitier",c2, new Date() , c2.getId(), null));
			Bill bill3 = billRepository.save(new Bill(null, "Viande", c3, new Date() , c3.getId(), null));

			productItemRepository.save(new ProductItem(null, "Carotte", null, p1.getId(), p1.getPrice(), 5.0, bill1));
			productItemRepository.save(new ProductItem(null, "Banane", null,p2.getId(), p2.getPrice(), 3.0, bill1));
			productItemRepository.save(new ProductItem(null, "Fraise", null, p3.getId(), p3.getPrice(), 1.0, bill1));

			productItemRepository.save(new ProductItem(null, "Yaourt", null, p3.getId(), p3.getPrice(), 233.0, bill2));
			productItemRepository.save(new ProductItem(null, "Lait demi-entier", null, p2.getId(), p2.getPrice(), 299.95, bill2));

			productItemRepository.save(new ProductItem(null, "cote de boeuf", null, p1.getId(), p1.getPrice(), 999.99, bill3));
		};
	}
}

//========================== MVC server side ====================//
@RestController
class BillRestController {
	@Autowired
	private BillRepository billRepository;
	@Autowired
	private ProductItemRepository productItemRepository;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private InventoryService inventoryService;

	@GetMapping("/fullBill/{id}")
	public Bill getBill(@PathVariable(name="id") Long id) {
		Bill bill = billRepository.findById(id).get();
		bill.setCustomer(customerService.findCustomerById(bill.getCustomerID()));
		bill.getProductItems().forEach(pi -> pi.setProduct(inventoryService.findProductById(pi.getProductID())));
		return bill;
	}
}
