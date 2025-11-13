package com._oormthon.seasonthon.domain.step.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * List<String> ↔ JSON 문자열 자동 변환용 Converter
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        try {
            return list == null ? "[]" : mapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new IllegalArgumentException("List<String> → JSON 변환 실패", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String json) {
        try {
            return json == null || json.isBlank()
                    ? new ArrayList<>()
                    : mapper.readValue(json, new TypeReference<List<String>>() {
                    });
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON → List<String> 변환 실패", e);
        }
    }
}