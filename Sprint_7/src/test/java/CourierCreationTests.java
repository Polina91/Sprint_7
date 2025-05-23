import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class CourierCreationTests {

    private String baseUrl = "https://qa-scooter.praktikum-services.ru/api/v1";
    private String courierLogin = "testCourier";
    private String courierPassword = "password123";
    private String courierFirstName = "TestName";
    private int createdCourierId = -1;

    @Before
    public void setUp() {
        RestAssured.baseURI = baseUrl;
    }

    @After
    public void tearDown() {
        if (createdCourierId != -1) {
            // Удаляем курьера после теста
            RestAssured
                    .given()
                    .contentType(ContentType.JSON)
                    .body("{\"login\":\"" + courierLogin + "\", \"password\":\"" + courierPassword + "\"}")
                    .when()
                    .post("/courier/login")
                    .then()
                    .statusCode(200);

            RestAssured
                    .given()
                    .when()
                    .delete("/courier/" + createdCourierId)
                    .then()
                    .statusCode(200);
        }
    }

    @Test
    public void createCourierSuccessfully() {
        String body = String.format("{\"login\":\"%s\", \"password\":\"%s\", \"firstName\":\"%s\"}",
                courierLogin, courierPassword, courierFirstName);

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true))
                .extract()
                .response();

        // Авторизация, чтобы получить id курьера для удаления
        Response loginResponse = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body("{\"login\":\"" + courierLogin + "\", \"password\":\"" + courierPassword + "\"}")
                .when()
                .post("/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract()
                .response();

        createdCourierId = loginResponse.path("id");
    }

    @Test
    public void cannotCreateDuplicateCourier() {
        // Сначала создаем курьера
        createCourierSuccessfully();

        // Пытаемся создать курьера с таким же логином
        String duplicateBody = String.format("{\"login\":\"%s\", \"password\":\"%s\", \"firstName\":\"%s\"}",
                courierLogin, courierPassword, courierFirstName);

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(duplicateBody)
                .when()
                .post("/courier")
                .then()
                .statusCode(anyOf(is(400), is(409)))  // В документации может быть 400 или 409 при конфликте
                .body("message", containsString("Этот логин уже используется"));
    }

    @Test
    public void cannotCreateCourierWithoutRequiredFields() {
        // Создаём без поля password
        String body = String.format("{\"login\":\"%s\", \"firstName\":\"%s\"}", courierLogin, courierFirstName);

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/courier")
                .then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных для создания учетной записи"));
    }
}
