package psu.lab13.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import psu.lab13.model.Product;
import psu.lab13.repositories.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	// Constructor injection: the dependency is final and the object cannot exist
	// half-built, which field injection cannot promise.
	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Iterable<Product> findAll() {
		return productRepository.findAll();
	}

	public Page<Product> findPage(Pageable pageable) {
		return productRepository.findAll(pageable);
	}

	public Optional<Product> findById(Integer id) {
		return productRepository.findById(id);
	}

	public long count() {
		return productRepository.count();
	}
}
