package psu.lab14.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import psu.lab14.model.Course;

@Repository
public interface CourseRepository
		extends CrudRepository<Course, Long>, PagingAndSortingRepository<Course, Long> {

	boolean existsByCodeIgnoreCase(String code);
}
