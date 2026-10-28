package psu.lab14.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import psu.lab14.model.Course;
import psu.lab14.services.CourseService;

/**
 * The route table from the handout, implemented exactly:
 *
 * <pre>
 * POST   /api/v1/courses        201 + Location header | 400 invalid | 422 id taken
 * GET    /api/v1/courses        200
 * GET    /api/v1/courses/{id}   200 | 404
 * PUT    /api/v1/courses/{id}   204 | 404
 * DELETE /api/v1/courses/{id}   204 | 404
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/courses")
@Validated
public class CourseController {

	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@GetMapping
	public ResponseEntity<Iterable<Course>> findAll() {
		return ResponseEntity.ok(courseService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Course> findById(
			@PathVariable @PositiveOrZero(message = "id cannot be negative") Long id) {

		return courseService.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	/**
	 * 422 rather than 400 when the id is already taken: the request is well-formed
	 * and understood, it just conflicts with the current state. 400 means "I could
	 * not parse this", which would be a lie here.
	 */
	@PostMapping
	public ResponseEntity<Void> create(@Valid @RequestBody Course course, UriComponentsBuilder uri) {
		if (courseService.exists(course.id())) {
			return ResponseEntity.unprocessableEntity().build();
		}

		Course saved = courseService.create(course);

		URI location = uri.path("/api/v1/courses/{id}").buildAndExpand(saved.id()).toUri();
		return ResponseEntity.created(location).build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> update(
			@PathVariable @PositiveOrZero(message = "id cannot be negative") Long id,
			@Valid @RequestBody Course course) {

		return courseService.update(id, course).isPresent()
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@PathVariable @PositiveOrZero(message = "id cannot be negative") Long id) {

		return courseService.delete(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}
}
