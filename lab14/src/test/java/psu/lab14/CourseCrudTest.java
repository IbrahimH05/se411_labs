package psu.lab14;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourseCrudTest {

	@Autowired
	private MockMvc mockMvc;

	private static final String BASE = "/api/v1/courses";

	private static String json(Long id, String code, String name, Integer credits) {
		String idPart = id == null ? "" : "\"id\":" + id + ",";
		return "{" + idPart + "\"code\":\"" + code + "\",\"name\":\"" + name
				+ "\",\"description\":\"test\",\"credits\":" + credits + "}";
	}

	// Read
	@Test
	@Order(1)
	void retrieve_all_courses_returns_200_and_the_seed_data() throws Exception {
		mockMvc.perform(get(BASE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(6)));
	}

	@Test
	@Order(2)
	void retrieve_by_id_returns_200() throws Exception {
		mockMvc.perform(get(BASE + "/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code", is("SE411")))
				.andExpect(jsonPath("$.credits", is(3)));
	}

	@Test
	@Order(3)
	void retrieve_an_unknown_id_returns_404() throws Exception {
		mockMvc.perform(get(BASE + "/9999")).andExpect(status().isNotFound());
	}

	// Create
	@Test
	@Order(4)
	void create_returns_201_with_a_location_header_and_the_row_really_exists() throws Exception {
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(50L, "SE450", "Created Course", 3)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString(BASE + "/50")));

		// The bug this guards: save() on a non-null id issues an UPDATE, matches no
		// rows, and still returns 201 with a Location pointing at nothing.
		mockMvc.perform(get(BASE + "/50"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Created Course")));
	}

	@Test
	@Order(5)
	void create_with_an_id_that_already_exists_returns_422() throws Exception {
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(1L, "DUP", "Duplicate id", 3)))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@Order(6)
	void create_without_an_id_lets_the_database_assign_one() throws Exception {
		// Guards the second bug: seeding explicit ids does not advance the IDENTITY
		// sequence, so an auto-assigned id would collide with the seed data.
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(null, "AUTO", "Auto assigned", 3)))
				.andExpect(status().isCreated());
	}

	// Update
	@Test
	@Order(7)
	void update_returns_204_and_the_change_is_visible() throws Exception {
		mockMvc.perform(put(BASE + "/50").contentType(MediaType.APPLICATION_JSON)
						.content(json(50L, "SE450", "Renamed Course", 5)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(BASE + "/50"))
				.andExpect(jsonPath("$.name", is("Renamed Course")))
				.andExpect(jsonPath("$.credits", is(5)));
	}

	@Test
	@Order(8)
	void update_an_absent_course_returns_404() throws Exception {
		mockMvc.perform(put(BASE + "/9999").contentType(MediaType.APPLICATION_JSON)
						.content(json(9999L, "NONE", "Nothing", 3)))
				.andExpect(status().isNotFound());
	}

	// Delete
	@Test
	@Order(9)
	void delete_returns_204_then_the_course_is_gone() throws Exception {
		mockMvc.perform(delete(BASE + "/50")).andExpect(status().isNoContent());
		mockMvc.perform(get(BASE + "/50")).andExpect(status().isNotFound());
	}

	@Test
	@Order(10)
	void delete_an_absent_course_returns_404() throws Exception {
		mockMvc.perform(delete(BASE + "/9999")).andExpect(status().isNotFound());
	}

	// The handout's four validation rules
	@Test
	@Order(11)
	void a_negative_id_is_rejected() throws Exception {
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(-1L, "NEG", "Negative id", 3)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.id", is("id cannot be negative")));
	}

	@Test
	@Order(12)
	void an_empty_code_is_rejected() throws Exception {
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(null, "", "Blank code", 3)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.code", is("code cannot be empty")));
	}

	@Test
	@Order(13)
	void a_name_shorter_than_two_characters_is_rejected() throws Exception {
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(null, "OK", "A", 3)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.name", is("name must be at least 2 characters")));
	}

	@Test
	@Order(14)
	void credits_outside_one_to_ten_are_rejected_at_both_ends() throws Exception {
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(null, "OK", "Zero credits", 0)))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(null, "OK", "Eleven credits", 11)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(15)
	void the_boundary_values_one_and_ten_are_accepted() throws Exception {
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(null, "MIN", "One credit", 1)))
				.andExpect(status().isCreated());

		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(null, "MAX", "Ten credits", 10)))
				.andExpect(status().isCreated());
	}

	@Test
	@Order(16)
	void every_violation_is_reported_at_once_not_one_at_a_time() throws Exception {
		mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
						.content(json(-1L, "", "A", 99)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.id").exists())
				.andExpect(jsonPath("$.errors.code").exists())
				.andExpect(jsonPath("$.errors.name").exists())
				.andExpect(jsonPath("$.errors.credits").exists());
	}
}
