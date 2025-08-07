package com.signature.autosave.infra.components.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signature.autosave.modules.pay.payment.dto.Subscription;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SubscriptionConverter implements AttributeConverter<Subscription, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Subscription attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar DTO para JSON", e);
        }
    }

    @Override
    public Subscription convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Subscription.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar JSON para DTO", e);
        }
    }
}

