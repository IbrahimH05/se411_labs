package psu.lab13.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

/**
 * Without this, a constraint violation on a path variable escapes as an unhandled
 * exception and Spring answers <b>500 Internal Server Error</b>. Nothing on the
 * server went wrong — the client sent a bad id — so the correct answer is 400.
 *
 * <p>Getting this wrong matters beyond tidiness: 5xx tells a caller "retry later,
 * it's us", and a client obeying that will retry a request that can never succeed.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	/** Violations on method parameters, e.g. {@code @Positive} on a path variable. */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> onConstraintViolation(ConstraintViolationException e) {
		Map<String, String> errors = new TreeMap<>();

		e.getConstraintViolations().forEach(violation -> {
			String path = violation.getPropertyPath().toString();
			String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
			errors.put(field, violation.getMessage());
		});

		return badRequest(errors);
	}

	/** Violations on an {@code @Valid} request body. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> onInvalidBody(MethodArgumentNotValidException e) {
		Map<String, String> errors = new TreeMap<>();

		e.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		return badRequest(errors);
	}

	private ResponseEntity<Map<String, Object>> badRequest(Map<String, String> errors) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
		body.put("errors", errors);

		return ResponseEntity.badRequest().body(body);
	}
}
