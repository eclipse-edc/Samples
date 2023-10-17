package org.eclipse.edc.samples.transfer;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static java.nio.file.Files.readString;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.*;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;

public class TransferUtil {

    public static Duration TIMEOUT = Duration.ofSeconds(30);
    public static Duration POLL_INTERVAL = Duration.ofMillis(500);

    public static void get(String url) {
        given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(url)
                .when()
                .get(url)
                .then()
                .log()
                .ifError()
                .statusCode(HttpStatus.SC_OK);
    }

    public static String get(String url, String jsonPath) {
        return given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(url)
                .when()
                .get(url)
                .then()
                .log()
                .ifError()
                .statusCode(HttpStatus.SC_OK)
                .body(jsonPath, not(emptyString()))
                .extract()
                .jsonPath()
                .get(jsonPath);
    }

    public static String get(String url, String authCode, String jsonPath) {
        return given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE, AUTHORIZATION, authCode)
                .contentType(ContentType.JSON)
                .body(url)
                .when()
                .get(url)
                .then()
                .log()
                .ifError()
                .statusCode(HttpStatus.SC_OK)
                .body(jsonPath, not(emptyString()))
                .extract()
                .jsonPath()
                .get(jsonPath);
    }

    public static void post(String url, String requestBody) {
        given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(url)
                .then()
                .log()
                .ifError()
                .statusCode(HttpStatus.SC_OK);
    }

    public static String post(String url, String requestBody, String jsonPath) {
        return given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(url)
                .then()
                .log()
                .ifError()
                .statusCode(HttpStatus.SC_OK)
                .body(jsonPath, not(emptyString()))
                .extract()
                .jsonPath()
                .get(jsonPath);
    }
}
