package psu.lab14.services;

import java.util.Optional;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;

import psu.lab14.model.Course;
import psu.lab14.repositories.CourseRepository;

@Service
public class CourseService {

	private final CourseRepository courseRepository;
	private final JdbcAggregateTemplate jdbcTemplate;

	public CourseService(CourseRepository courseRepository, JdbcAggregateTemplate jdbcTemplate) {
		this.courseRepository = courseRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	public Iterable<Course> findAll() {
		return courseRepository.findAll();
	}

	public Optional<Course> findById(Long id) {
		return courseRepository.findById(id);
	}

	public boolean exists(Long id) {
		return id != null && courseRepository.existsById(id);
	}

	/**
	 * {@code repository.save()} is an upsert that decides INSERT or UPDATE from
	 * whether the id is null. A new course that arrives <em>with</em> an id would
	 * therefore be issued as an UPDATE, match no rows, and be silently discarded —
	 * save() returns the object either way, so the caller sees success and the
	 * database sees nothing.
	 *
	 * <p>{@code insert()} always issues an INSERT, which is what "create" means.
	 */
	public Course create(Course course) {
		return jdbcTemplate.insert(course);
	}

	/**
	 * Spring Data JDBC's {@code save} is an upsert keyed on the id, so an update has
	 * to be explicit about which id it is writing — the body's id is ignored in
	 * favour of the one in the URL, which is the resource being addressed.
	 */
	public Optional<Course> update(Long id, Course incoming) {
		if (!courseRepository.existsById(id)) {
			return Optional.empty();
		}

		Course merged = new Course(id, incoming.code(), incoming.name(),
				incoming.description(), incoming.credits());

		// save() on an existing id is an UPDATE, which is correct here.
		return Optional.of(courseRepository.save(merged));
	}

	public boolean delete(Long id) {
		if (!courseRepository.existsById(id)) {
			return false;
		}
		courseRepository.deleteById(id);
		return true;
	}
}
