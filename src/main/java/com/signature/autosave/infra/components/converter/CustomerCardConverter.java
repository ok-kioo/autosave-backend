package com.signature.autosave.infra.components.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.resources.customer.CustomerCard;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CustomerCardConverter implements AttributeConverter<CustomerCard, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CustomerCard attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar DTO para JSON", e);
        }
    }

    @Override
    public CustomerCard convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, CustomerCard.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar JSON para DTO", e);
        }
    }
}
