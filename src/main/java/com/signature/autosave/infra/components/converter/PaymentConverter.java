package com.signature.autosave.infra.components.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.resources.payment.Payment;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PaymentConverter implements AttributeConverter<Payment, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Payment attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar DTO para JSON", e);
        }
    }

    @Override
    public Payment convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Payment.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar JSON para DTO", e);
        }
    }
}
