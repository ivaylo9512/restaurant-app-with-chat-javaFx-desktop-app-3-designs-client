package exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

import static application.ServerRequests.mapper;

public class UnprocessableEntityException extends RuntimeException {
    private final Map<String, String> fieldErrors;
    private final String message;

    public UnprocessableEntityException(String message) throws JsonProcessingException {
        fieldErrors = mapper.readValue(message, new TypeReference<>() {});
        this.message = String.join("\n", fieldErrors.values());
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
