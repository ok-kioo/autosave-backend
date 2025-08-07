package com.signature.autosave.infra.components.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.resources.customer.Customer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CustomerConverter implements AttributeConverter<Customer, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Customer attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar DTO para JSON", e);
        }
    }

    @Override
    public Customer convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Customer.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar JSON para DTO", e);
        }
    }
}
