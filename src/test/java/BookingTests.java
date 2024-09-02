import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static io.restassured.RestAssured.given;


public class BookingTests {

    private final String BASE_URL = "http://restful-booker.herokuapp.com";

    public String getAuthToken() {
        String authPayload = "{ \"username\" : \"admin\", \"password\" : \"password123\" }";

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .body(authPayload)
                .when()
                .post(BASE_URL + "/auth")
                .then()
                .statusCode(200)
                .extract().response();

        return response.jsonPath().getString("token");
    }

    @Test
    public void testCreateBooking() {
        Booking booking = createSampleBooking();

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .body(booking)
                .when()
                .post(BASE_URL + "/booking")
                .then()
                .statusCode(200)
                .extract().response();

        int bookingId = response.jsonPath().getInt("bookingid");
        assertNotNull(bookingId);
    }

    @Test
    public void testGetAllBookingIds() {
        Response response = given()
                .header("Accept", "application/json")
                .when()
                .get(BASE_URL + "/booking")
                .then()
                .statusCode(200)
                .extract().response();

        List<Integer> bookingIds = response.jsonPath().getList("bookingid");
        assertNotNull(bookingIds);
    }

    @Test
    public void testUpdateBookingPrice() {
        String token = getAuthToken();
        int bookingId = getBookingId();

        Booking updatedBooking = createSampleBooking();
        updatedBooking.setTotalprice(1500); // нова ціна

        given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updatedBooking)
                .when()
                .patch(BASE_URL + "/booking/" + bookingId)
                .then()
                .statusCode(200);
    }


    @Test
    public void testUpdateBookingNameAndNeeds() {
        int bookingId = getBookingId();

        Booking updatedBooking = createSampleBooking();
        updatedBooking.setFirstname("John");
        updatedBooking.setAdditionalneeds("Late checkout");

        given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .body(updatedBooking)
                .when()
                .put(BASE_URL + "/booking/" + bookingId)
                .then()
                .statusCode(200);

        Booking updatedBookingResponse = given()
                .header("Accept", "application/json")
                .when()
                .get(BASE_URL + "/booking/" + bookingId)
                .then()
                .statusCode(200)
                .extract().as(Booking.class);

        assertEquals("John", updatedBookingResponse.getFirstname());
        assertEquals("Late checkout", updatedBookingResponse.getAdditionalneeds());
    }

    @Test
    public void testDeleteBooking() {
        String token = getAuthToken();
        int bookingId = getBookingId();

        given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(BASE_URL + "/booking/" + bookingId)
                .then()
                .statusCode(201);

        given()
                .header("Accept", "application/json")
                .when()
                .get(BASE_URL + "/booking/" + bookingId)
                .then()
                .statusCode(404);
    }

    private Booking createSampleBooking() {
        Booking booking = new Booking();
        booking.setFirstname("Test");
        booking.setLastname("User");
        booking.setTotalprice(1200);
        booking.setDepositpaid(true);

        BookingDates bookingDates = new BookingDates();
        LocalDate checkin = LocalDate.parse("2024-09-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate checkout = LocalDate.parse("2024-09-10", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        bookingDates.setCheckin(java.sql.Date.valueOf(checkin));
        bookingDates.setCheckout(java.sql.Date.valueOf(checkout));

        booking.setBookingdates(bookingDates);
        booking.setAdditionalneeds("Breakfast");

        return booking;
    }

    private int getBookingId() {
        Response response = given()
                .header("Accept", "application/json")
                .when()
                .get(BASE_URL + "/booking")
                .then()
                .statusCode(200)
                .extract().response();

        List<Integer> bookingIds = response.jsonPath().getList("bookingid");
        return bookingIds.get(0);
    }
}
