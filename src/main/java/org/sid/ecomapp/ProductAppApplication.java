package org.sid.ecomapp;

import lombok.Data;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade;
import org.sid.ecomapp.entities.Product;
import org.sid.ecomapp.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Supplier;

@Controller
class productController{
	@Autowired
	private ProductRepository productRepository;

	@GetMapping("/")
	public String index(){
		return "index";
	}
	@GetMapping("/products")
	public String index(Model model){

		model.addAttribute("products",productRepository.findAll());
		return "products";
	}
}
@Controller
class supplierController{
	@Autowired
	private KeycloakRestTemplate RestTemplate;

	@GetMapping("/")
	public String index(){
		return "index";
	}
	@GetMapping("/suppliers")
	public String index(Model model){
		ResponseEntity<PagedModel<Supplier>> response=
				RestTemplate.exchange("http://localhost:8083/suppliers", HttpMethod.GET, null,
						new ParameterizedTypeReference<PagedModel<Supplier>>() { });
		model.addAttribute("suppliers",response.getBody().getContent());
		return "suppliers";
	}
}
@Data
class supplier{
	private Long id;
	private String name;
	private String email;
}

@SpringBootApplication
public class ProductAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductAppApplication.class, args);
	}

	@Bean
	CommandLineRunner start(ProductRepository productRepository, RepositoryRestConfiguration restConfiguration){
		return args -> {
			restConfiguration.exposeIdsFor(Product.class);
			productRepository.save(new Product(null,"PS4",1654));
			productRepository.save(new Product(null,"PS3",1100));
			productRepository.save(new Product(null,"PS2",800));
			productRepository.findAll().forEach(product -> {
				System.out.println(product.getName());
			});
		};
	}

}
@Controller
class SecurityController{
	@Autowired
	private AdapterDeploymentContext adapterDeploymentContext;

	@GetMapping("/logout")
	public String logout(HttpServletRequest request) throws ServletException{
		request.logout();
		return "redirect:/";
	}
	@GetMapping("/changePassword")
	public String cpw(RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) throws ServletException{
		HttpFacade facade= new SimpleHttpFacade(request, response);
		KeycloakDeployment deployment = adapterDeploymentContext.resolveDeployment(facade);
		redirectAttributes.addAttribute("referrer", deployment.getResourceName());
		return "redirect:" + deployment.getAccountUrl() + "/password";
	}
}
