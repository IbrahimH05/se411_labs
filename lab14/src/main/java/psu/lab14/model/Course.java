package psu.lab14.model;

import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * The validation rules the handout lists, as annotations on the record components.
 *
 * <p>{@code id} is {@code @PositiveOrZero} rather than {@code @Positive}: the rule is
 * "cannot be negative", and a null id is also allowed, because POSTing without one is
 * how you ask the database to assign it.
 */
public record Course(

		@Id
		@PositiveOrZero(message = "id cannot be negative")
		Long id,

		@NotBlank(message = "code cannot be empty")
		@Size(max = 20, message = "code must be at most 20 characters")
		String code,

		@NotBlank(message = "name is required")
		@Size(min = 2, max = 100, message = "name must be at least 2 characters")
		String name,

		@Size(max = 255, message = "description must be at most 255 characters")
		String description,

		@NotNull(message = "credits is required")
		@Min(value = 1, message = "credits must be at least 1")
		@Max(value = 10, message = "credits must be at most 10")
		Integer credits) {
}
