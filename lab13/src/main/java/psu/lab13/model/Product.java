package psu.lab13.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Price is BigDecimal, not Double: 0.1 + 0.2 != 0.3 in binary floating point, and a
 * catalogue that cannot add up its own prices is not much of a catalogue. The column
 * is DECIMAL(10,2) to match.
 */
public record Product(

		@Id
		@Positive(message = "id must be positive")
		Integer id,

		@NotBlank(message = "name is required")
		@Size(max = 100, message = "name must be at most 100 characters")
		String name,

		@Size(max = 100, message = "description must be at most 100 characters")
		String description,

		@NotNull(message = "price is required")
		@DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than zero")
		BigDecimal price) {
}
