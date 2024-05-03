package ru.polyakov.bookstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
abstract class MockMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    protected String expectedFrom(MockHttpServletRequestBuilder builder, HttpStatus expectedStatus) throws Exception {
        return mockMvc.perform(builder)
                .andExpect(status().is(expectedStatus.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    protected void expectedMessageAndStatusFrom(MockHttpServletRequestBuilder builder, HttpStatus status, String message) throws Exception {
        mockMvc.perform(builder)
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(message));
    }

    protected void expectedFieldsAndStatusFrom(MockHttpServletRequestBuilder builder, HttpStatus status, String... fields) throws Exception {
        int length = fields.length;
        if((length & 1) == 1) throw new IllegalArgumentException("field cannot be without value");

        ResultMatcher[] matchers = new ResultMatcher[(fields.length >>> 1) + 1];

        for (int i = 0; i < length; i+=2) {
            matchers[i >>> 1] = jsonPath("$.".concat(fields[i])).value(fields[i + 1]);
        }

        matchers[matchers.length - 1] = status().is(status.value());
        mockMvc.perform(builder)
                .andExpectAll(matchers);
    }

    protected MultiValueMap<String, String> createParams(String... params) {
        if((params.length & 1) == 1) throw new IllegalArgumentException("param must had a value");
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        for(int i = 0; i < params.length; i+=2) {
            multiValueMap.add(params[i], params[i + 1]);
        }
        return multiValueMap;
    }
}
