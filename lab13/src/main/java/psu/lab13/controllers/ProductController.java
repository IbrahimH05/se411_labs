package psu.lab13.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import psu.lab13.model.Product;
import psu.lab13.services.ProductService;

/**
 * {@code @Validated} on the class is what makes the {@code @Positive} on a path
 * variable actually run. Without it the annotation is silently ignored — the
 * constraint only applies to method parameters when the class is a validation proxy.
 */
@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public ResponseEntity<Iterable<Product>> findAll() {
		return ResponseEntity.ok(productService.findAll());
	}

	@GetMapping("/paged")
	public ResponseEntity<Page<Product>> findPage(Pageable pageable) {
		return ResponseEntity.ok(productService.findPage(pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> findById(@PathVariable @Positive(message = "id must be positive") Integer id) {
		return productService.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
