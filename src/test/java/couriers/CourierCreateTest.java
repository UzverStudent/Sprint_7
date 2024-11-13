package couriers;

import service.Service;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import io.restassured.RestAssured;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.is;

public class CourierCreateTest {

    private final CourierAPI courierApi = new CourierAPI();
    private final CourierGenerator courierGenerator = new CourierGenerator();
    private Courier courierRandom;

    @Before
    public void setUp() {
        RestAssured.baseURI = Service.BASE_URL;
        courierRandom = courierGenerator.generateRandom();
    }

    @After
    public void tearDown() {
        try {
            Response responseLogin = courierLogin(courierRandom);
            String courierId = responseLogin.then().extract().path("id").toString();
            courierDelete(courierId);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    @DisplayName("Регистрация нового курьера")
    @Description("Cоздание нового курьера с корректыми введенными данными")
    public void createNewCourierPositiveTest() {
        Response response = courierCreate(courierRandom);
        compareResultToTrue(response, SC_CREATED);
    }

    @Test
    @DisplayName("Регистрация нового курьера без логина")
    @Description("Невозможность создания нового курьера без указания логина")
    public void createNewCourierNoLoginTest() {
        courierRandom.setLogin("");
        Response response = courierCreate(courierRandom);
        compareResultMessageToText(response, SC_BAD_REQUEST, "Недостаточно данных для создания учетной записи");
    }

    @Test
    @DisplayName("Регистрация нового повторяющегося курьера")
    @Description("Невозможность создания нового курьера с логином, который уже существует")
    public void createNewDuplicateCourierTest() {
        courierCreate(courierRandom);
        Response response = courierCreate(courierRandom);
        compareResultMessageToText(response, SC_CONFLICT, "Этот логин уже используется. Попробуйте другой.");
    }

    @Step("Create courier")
    public Response courierCreate(Courier courier){
        Response response = courierApi.createCourier(courier);
        printResponseBodyToConsole("Создание курьера: ", response, Service.NEED_DETAIL_LOG);
        return response;
    }

    @Step("Login courier")
    public Response courierLogin(Courier courier){
        Response response = courierApi.login(courier);
        printResponseBodyToConsole("Авторизация курьера: ", response, Service.NEED_DETAIL_LOG);
        return response;
    }

    @Step("Delete courier by id")
    public void courierDelete(String courierId){
        Response response = courierApi.delete(courierId);
        printResponseBodyToConsole("Удаление курьера: ", response, Service.NEED_DETAIL_LOG);
    }

    @Step("Compare result to true")
    public void compareResultToTrue(Response response, int statusCode){
        response
                .then()
                .assertThat()
                .log().all()
                .statusCode(statusCode)
                .body("ok", is(true));
    }

    @Step("Compare result message to something")
    public void compareResultMessageToText(Response response, int statusCode, String text){
        response
                .then()
                .log().all()
                .statusCode(statusCode)
                .and()
                .assertThat()
                .body("message", is(text));
    }

    @Step("Print response body to console")
    public void printResponseBodyToConsole(String headerText, Response response, boolean detailedLog){
        if (detailedLog)
            System.out.println(headerText + response.body().asString());
    }

}