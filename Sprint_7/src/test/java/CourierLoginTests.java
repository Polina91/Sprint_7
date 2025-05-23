import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class CourierLoginTests {

    private String baseUrl = "https://qa-scooter.praktikum-services.ru/api/v1";
    private String courierLogin = "loginCourier";
    private String courierPassword = "secret123";
    private String courierFirstName = "Autotest";
    private int courierId = -1;

    @Before
    public void setUp() {
        RestAssured.baseURI = baseUrl;

        // Создание курьера перед тестами
        String body = String.format("{\"login\":\"%s\", \"password\":\"%s\", \"firstName\":\"%s\"}",
                courierLogin, courierPassword, courierFirstName);

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/courier");

        // Получение id
        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", courierLogin, courierPassword))
                .post("/courier/login");

        courierId = response.path("id");
    }

    @After
    public void tearDown() {
        if (courierId != -1) {
            RestAssured
                    .given()
                    .delete("/courier/" + courierId);
        }
    }

    @Test
    public void courierCanLoginWithValidCredentials() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", courierLogin, courierPassword))
                .when()
                .post("/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    public void loginFailsWithWrongPassword() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"login\":\"%s\", \"password\":\"wrongpass\"}", courierLogin))
                .when()
                .post("/courier/login")
                .then()
                .statusCode(404)
                .body("message", containsString("Учетная запись не найдена"));
    }

    @Test
    public void loginFailsWithWrongLogin() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"login\":\"wronglogin\", \"password\":\"%s\"}", courierPassword))
                .when()
                .post("/courier/login")
                .then()
                .statusCode(404)
                .body("message", containsString("Учетная запись не найдена"));
    }

    @Test
    public void loginFailsWithoutPassword() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"login\":\"%s\"}", courierLogin))
                .when()
                .post("/courier/login")
                .then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных для входа"));
    }

    @Test
    public void loginFailsWithoutLogin() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"password\":\"%s\"}", courierPassword))
                .when()
                .post("/courier/login")
                .then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных для входа"));
    }

    @Test
    public void loginFailsForNonExistentCourier() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body("{\"login\":\"nonexistent\", \"password\":\"whatever\"}")
                .when()
                .post("/courier/login")
                .then()
                .statusCode(404)
                .body("message", containsString("Учетная запись не найдена"));
    }
}
