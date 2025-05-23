import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.Order;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderTest {

    private final List<String> color;

    public CreateOrderTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters(name = "Цвет: {0}")
    public static Collection<Object[]> getColors() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList("BLACK")},
                {Arrays.asList("GREY")},
                {Arrays.asList("BLACK", "GREY")},
                {Arrays.asList()}  // без цвета
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/api/v1";
    }

    @Test
    public void createOrderWithDifferentColors() {
        Order order = new Order(
                "Имя", "Фамилия", "ул. Пример 1", "4", "+79991234567",
                5, "2025-06-01", "Комментарий", color
        );

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .body("track", notNullValue())
                .extract()
                .response();

        int track = response.path("track");
        System.out.println("Создан заказ, track: " + track);
    }
}
