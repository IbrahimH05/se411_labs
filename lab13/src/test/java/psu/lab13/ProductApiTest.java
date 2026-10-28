package psu.lab13;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void the_context_loads_and_seed_data_is_present() throws Exception {
		mockMvc.perform(get("/api/v1/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(17)));
	}

	@Test
	void a_known_product_is_returned_with_200() throws Exception {
		mockMvc.perform(get("/api/v1/products/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(10)))
				.andExpect(jsonPath("$.name", is("3d printer")))
				.andExpect(jsonPath("$.price", is(1840.25)));
	}

	@Test
	void an_unknown_id_is_404_not_an_empty_200() throws Exception {
		mockMvc.perform(get("/api/v1/products/9999"))
				.andExpect(status().isNotFound());
	}

	/** The handout's question: a negative id must be 400, not 500. */
	@Test
	void a_negative_id_is_rejected_with_400_and_names_the_field() throws Exception {
		mockMvc.perform(get("/api/v1/products/-5"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.errors.id", is("id must be positive")));
	}

	@Test
	void zero_is_also_not_positive() throws Exception {
		mockMvc.perform(get("/api/v1/products/0"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void a_non_numeric_id_is_a_client_error_too() throws Exception {
		mockMvc.perform(get("/api/v1/products/abc"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void prices_keep_two_decimal_places_through_bigdecimal() throws Exception {
		// 1210.25 survives the round trip exactly; a double column would not promise that.
		mockMvc.perform(get("/api/v1/products/13"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price", is(1210.25)));
	}

	@Test
	void paging_and_sorting_come_from_the_repository_for_free() throws Exception {
		mockMvc.perform(get("/api/v1/products/paged?size=3&sort=price,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(3)))
				.andExpect(jsonPath("$.totalElements", is(17)))
				.andExpect(jsonPath("$.content[0].price", is(3640.25)));
	}
}
