package edu.spu.se411.lab07_polymorphism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.spu.se411.lab07_polymorphism.config.TravelConfig;
import edu.spu.se411.lab07_polymorphism.exceptions.BookingException;
import edu.spu.se411.lab07_polymorphism.exceptions.InvalidArgumentException;
import edu.spu.se411.lab07_polymorphism.exceptions.MissingInformationException;
import edu.spu.se411.lab07_polymorphism.model.Booking;
import edu.spu.se411.lab07_polymorphism.model.CarRentalBooking;
import edu.spu.se411.lab07_polymorphism.model.FlightBooking;
import edu.spu.se411.lab07_polymorphism.model.SeatClass;
import edu.spu.se411.lab07_polymorphism.model.TrainBooking;
import edu.spu.se411.lab07_polymorphism.service.BookingService;

public class BookingTest {

	private static final LocalDate TRAVEL_DATE = LocalDate.of(2026, 11, 12);
	private static final double DELTA = 0.0001;

	private final BookingService service = new BookingService();

	private FlightBooking flight() {
		return new FlightBooking("FL-1", "Ibrahim Alhalaki", TRAVEL_DATE, "Istanbul", 1000.00);
	}

	private TrainBooking train(SeatClass seatClass) {
		return new TrainBooking("TR-1", "Muhammad Abanmi", TRAVEL_DATE, "Riyadh", seatClass);
	}

	private CarRentalBooking car() {
		return new CarRentalBooking("CR-1", "Layla Nasser", TRAVEL_DATE, "Dubai", 100.00);
	}

	// Polymorphism
	@Test
	public void one_method_prices_every_booking_subtype() throws Exception {
		FlightBooking f = flight();
		f.setLuggageWeightKg(10.0);

		TrainBooking t = train(SeatClass.STANDARD);
		t.setDistanceKm(200.0);

		CarRentalBooking c = car();
		c.setRentalDays(3);

		List<Booking> bookings = List.of(f, t, c);

		// Same call, three different formulas, resolved at runtime.
		double[] totals = new double[3];
		for (int i = 0; i < bookings.size(); i++) {
			totals[i] = service.computeTotalPrice(bookings.get(i));
		}

		assertEquals(1000 + 10 * TravelConfig.EXTRA_LUGGAGE_RATE, totals[0], DELTA);
		assertEquals(200 * TravelConfig.TRAIN_STANDARD_RATE, totals[1], DELTA);
		assertEquals(100 * 3, totals[2], DELTA);
	}

	@Test
	public void every_subtype_reports_its_own_type_name() {
		assertEquals("Flight", flight().getBookingType());
		assertEquals("Train", train(SeatClass.STANDARD).getBookingType());
		assertEquals("Car rental", car().getBookingType());
	}

	@Test
	public void summary_is_inherited_but_uses_the_subtype_details() throws Exception {
		FlightBooking f = flight();
		f.setLuggageWeightKg(20.0);

		String summary = f.summary();
		assertTrue(summary.contains("FL-1"));
		assertTrue(summary.contains("Ibrahim Alhalaki"));
		assertTrue(summary.contains("Istanbul"));
		assertTrue(summary.contains("luggage 20.0 kg"));
	}

	// Flight pricing
	@Test
	public void flight_price_adds_the_extra_luggage_charge() throws Exception {
		FlightBooking f = flight();
		f.setLuggageWeightKg(23.5);

		assertEquals(1000 + 23.5 * TravelConfig.EXTRA_LUGGAGE_RATE, f.computeTotalPrice(), DELTA);
	}

	@Test
	public void flight_accepts_both_ends_of_the_luggage_range() {
		assertDoesNotThrow(() -> {
			FlightBooking min = flight();
			min.setLuggageWeightKg(TravelConfig.MIN_LUGGAGE_WEIGHT);
			min.computeTotalPrice();

			FlightBooking max = flight();
			max.setLuggageWeightKg(TravelConfig.MAX_LUGGAGE_WEIGHT);
			max.computeTotalPrice();
		});
	}

	@Test
	public void flight_rejects_luggage_outside_the_range() {
		assertThrows(InvalidArgumentException.class, () -> flight().setLuggageWeightKg(40.1));
		assertThrows(InvalidArgumentException.class, () -> flight().setLuggageWeightKg(-0.1));
	}

	@Test
	public void flight_without_luggage_weight_cannot_be_priced() {
		MissingInformationException thrown =
				assertThrows(MissingInformationException.class, () -> flight().computeTotalPrice());

		assertEquals("FL-1", thrown.getBookingId());
		assertEquals("Luggage weight", thrown.getFieldName());
	}

	// Train pricing
	@Test
	public void train_price_depends_on_the_seat_class() throws Exception {
		TrainBooking standard = train(SeatClass.STANDARD);
		standard.setDistanceKm(400.0);

		TrainBooking first = train(SeatClass.FIRST_CLASS);
		first.setDistanceKm(400.0);

		assertEquals(400 * TravelConfig.TRAIN_STANDARD_RATE, standard.computeTotalPrice(), DELTA);
		assertEquals(400 * TravelConfig.TRAIN_FIRST_CLASS_RATE, first.computeTotalPrice(), DELTA);
		assertTrue(first.computeTotalPrice() > standard.computeTotalPrice());
	}

	@Test
	public void train_accepts_both_ends_of_the_distance_range() {
		assertDoesNotThrow(() -> {
			TrainBooking min = train(SeatClass.STANDARD);
			min.setDistanceKm(TravelConfig.MIN_TRAIN_DISTANCE);
			min.computeTotalPrice();

			TrainBooking max = train(SeatClass.STANDARD);
			max.setDistanceKm(TravelConfig.MAX_TRAIN_DISTANCE);
			max.computeTotalPrice();
		});
	}

	@Test
	public void train_rejects_distance_outside_the_range() {
		assertThrows(InvalidArgumentException.class, () -> train(SeatClass.STANDARD).setDistanceKm(0.0));
		assertThrows(InvalidArgumentException.class, () -> train(SeatClass.STANDARD).setDistanceKm(2000.1));
	}

	@Test
	public void train_without_distance_cannot_be_priced() {
		MissingInformationException thrown = assertThrows(MissingInformationException.class,
				() -> train(SeatClass.FIRST_CLASS).computeTotalPrice());

		assertEquals("Distance", thrown.getFieldName());
	}

	// Car rental pricing
	@Test
	public void car_price_is_rate_times_days() throws Exception {
		CarRentalBooking c = car();
		c.setRentalDays(6);

		assertEquals(600.0, c.computeTotalPrice(), DELTA);
	}

	@Test
	public void car_accepts_both_ends_of_the_day_range() {
		assertDoesNotThrow(() -> {
			CarRentalBooking min = car();
			min.setRentalDays(TravelConfig.MIN_RENTAL_DAYS);
			min.computeTotalPrice();

			CarRentalBooking max = car();
			max.setRentalDays(TravelConfig.MAX_RENTAL_DAYS);
			max.computeTotalPrice();
		});
	}

	@Test
	public void car_rejects_days_outside_the_range() {
		assertThrows(InvalidArgumentException.class, () -> car().setRentalDays(0));
		assertThrows(InvalidArgumentException.class, () -> car().setRentalDays(31));
	}

	@Test
	public void car_without_days_cannot_be_priced() {
		assertThrows(MissingInformationException.class, () -> car().computeTotalPrice());
	}

	// Exceptions
	@Test
	public void both_custom_exceptions_share_one_base_type() {
		assertThrows(BookingException.class, () -> flight().computeTotalPrice());
		assertThrows(BookingException.class, () -> car().setRentalDays(99));
	}

	@Test
	public void invalid_argument_exception_carries_the_offending_value_and_bounds() {
		InvalidArgumentException thrown =
				assertThrows(InvalidArgumentException.class, () -> flight().setLuggageWeightKg(55.0));

		assertEquals(55.0, thrown.getValue(), DELTA);
		assertEquals(TravelConfig.MIN_LUGGAGE_WEIGHT, thrown.getMin(), DELTA);
		assertEquals(TravelConfig.MAX_LUGGAGE_WEIGHT, thrown.getMax(), DELTA);
	}

	// Shared validation inherited from Booking
	@Test
	public void common_fields_are_validated_once_for_every_subtype() {
		assertThrows(IllegalArgumentException.class,
				() -> new FlightBooking("", "Name", TRAVEL_DATE, "Istanbul", 100));
		assertThrows(IllegalArgumentException.class,
				() -> new TrainBooking("TR-1", "  ", TRAVEL_DATE, "Riyadh", SeatClass.STANDARD));
		assertThrows(IllegalArgumentException.class,
				() -> new CarRentalBooking("CR-1", "Name", null, "Dubai", 100));
		assertThrows(IllegalArgumentException.class,
				() -> new CarRentalBooking("CR-1", "Name", TRAVEL_DATE, "Dubai", -5));
	}

	// Service
	@Test
	public void revenue_skips_bookings_that_cannot_be_priced_yet() throws Exception {
		FlightBooking f = flight();
		f.setLuggageWeightKg(10.0);

		CarRentalBooking c = car();
		c.setRentalDays(2);

		List<Booking> bookings = List.of(f, c, train(SeatClass.STANDARD));

		assertEquals(2, service.findPriceable(bookings).size());
		assertEquals(1125.0 + 200.0, service.computeTotalRevenue(bookings), DELTA);
	}

	@Test
	public void service_rejects_a_null_booking() {
		assertThrows(IllegalArgumentException.class, () -> service.computeTotalPrice(null));
	}
}
