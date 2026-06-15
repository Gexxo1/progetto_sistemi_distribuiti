package com.progetto_sistemi_distribuiti.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTests {
    @NonNull
    private static final String APPLICATION_JSON = "application/json";

    @NonNull
    private static final String TEXT_PLAIN = "text/plain";

    @NonNull
    private static final String REVIEW_TEXT = "Recensione inserita dal test automatico";

    @NonNull
    private static final String UNAUTHORIZED_REVIEW_TEXT = "Tentativo non autorizzato";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginShouldReturnJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(Objects.requireNonNull(loginJson("luigi", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void reviewerShouldAddReview() throws Exception {
        String token = loginAndGetToken("luigi", "password");

        mockMvc.perform(post("/api/products/1/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(TEXT_PLAIN)
                        .content(REVIEW_TEXT))
                .andExpect(status().isOk());
    }

    @Test
    void normalUserShouldNotAddReview() throws Exception {
        String token = loginAndGetToken("mario", "password");

        mockMvc.perform(post("/api/products/1/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(TEXT_PLAIN)
                        .content(UNAUTHORIZED_REVIEW_TEXT))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldUpdatePrice() throws Exception {
        String token = loginAndGetToken("prof", "password");

        mockMvc.perform(put("/api/products/1/price")
                        .header("Authorization", "Bearer " + token)
                        .param("price", "99.99"))
                .andExpect(status().isOk());
    }

    @Test
    void productDebugDetailShouldRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isForbidden());

        String reviewerToken = loginAndGetToken("luigi", "password");
        mockMvc.perform(get("/api/products/1")
                        .header("Authorization", "Bearer " + reviewerToken))
                .andExpect(status().isForbidden());

        String adminToken = loginAndGetToken("prof", "password");
        mockMvc.perform(get("/api/products/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(Objects.requireNonNull(loginJson(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String loginJson(String username, String password) {
        return "{\n"
                + "  \"username\": \"" + username + "\",\n"
                + "  \"password\": \"" + password + "\"\n"
                + "}";
    }
}
