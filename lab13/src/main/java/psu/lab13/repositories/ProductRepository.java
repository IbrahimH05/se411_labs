package psu.lab13.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import psu.lab13.model.Product;

/**
 * No implementation is written: Spring Data generates one at startup and puts it in
 * the IoC container. Extending both interfaces is deliberate — since Spring Data 3,
 * PagingAndSortingRepository no longer extends CrudRepository, so neither alone gives
 * CRUD plus paging.
 */
@Repository
public interface ProductRepository
		extends CrudRepository<Product, Integer>, PagingAndSortingRepository<Product, Integer> {
}
