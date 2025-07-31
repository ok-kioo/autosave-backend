/*
package com.signature.autosave.persistence.repository;

import com.signature.autosave.persistence.builder.PaymentBuilder;
import com.signature.autosave.persistence.entity.Payment;
import com.signature.autosave.persistence.entity.User;
import com.signature.autosave.persistence.jpa.JPAManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

public class PaymentRepository implements Repository{
    @Getter
    private static final PaymentRepository instance = new PaymentRepository();
    private final EntityManager entityManager;
    private static final Logger LOGGER = Logger.getLogger(PaymentRepository.class.getName());

    private PaymentRepository() {
        this.entityManager = JPAManager.getInstance().getEntityManager();
    }

    @Override
    public Boolean create(Object[] params) {
        UUID payingId = UUID.fromString((String) params[0]);
        String paymentMethod = (String) params[1];
        String paymentStatus = (String) params[2];
        LocalDateTime paidAt = (LocalDateTime) params[3];
        LocalDateTime expiresAt = (LocalDateTime) params[4];

        User userId = entityManager.find(User.class, payingId);
        if (userId == null) {
            throw new IllegalArgumentException("User not found with id: " + payingId);
        }
        Payment payment = PaymentBuilder.builder()
                .withPaymentMethod(paymentMethod)
                .withStatus(paymentStatus)
                .withPaymentMade(paidAt)
                .withPaymentExpires(expiresAt)
                .withPaying(userId)
                .build();

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(payment);
            transaction.commit();
            LOGGER.info("Payment made successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - made payment: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return true;
    }

    @Override
    public Object list(Object[] params) {
        UUID payingId = UUID.fromString((String) params[0]);
        TypedQuery<Payment> query = entityManager.createQuery(
                "SELECT u FROM Payment u WHERE u.userId = :userId", Payment.class);
        query.setParameter("userId", payingId);
        try {
            Payment payment = query.getSingleResult();
            return payment.getId();
        } catch (NoResultException e) {
            LOGGER.warning("Payments not found for user with id: " + payingId);
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return null;
    }

    @Override
    public Boolean update(Object[] params) {
        UUID id = UUID.fromString((String) params[0]);
        String newPaymentStatus = (String) params[2];
        LocalDateTime newPaidAt = (LocalDateTime) params[3];
        LocalDateTime newExpiresAt = (LocalDateTime) params[4];

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Payment payment = entityManager.find(Payment.class, id);

            payment.setPaymentStatus(newPaymentStatus);
            payment.setPaidAt(newPaidAt);
            payment.setExpiresAt(newExpiresAt);

            transaction.commit();
            LOGGER.info("Payment updated successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - update payment: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return true;
    }

    @Override
    public Boolean delete(Object[] params) {
        UUID id = UUID.fromString((String) params[0]);

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            Payment payment = entityManager.find(Payment.class, id);

            entityManager.remove(payment);
            transaction.commit();
            LOGGER.info("Payment deleted successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - delete payment: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return true;
    }

    @Override
    public Object findById(Object[] params) {
        return null;
    }

    @Override
    public UUID findByEmail(String email) {
        return null;
    }

    @Override
    public Object getData(UUID id, String dataToGet) {
        Payment payment = entityManager.find(Payment.class, id);
        if (payment == null) {
            return null;
        }
        return switch (dataToGet) {
            case "method" -> payment.getPaymentMethod();
            case "status" -> payment.getPaymentStatus();
            case "paidAt" -> payment.getPaidAt();
            case "expiresAt" -> payment.getExpiresAt();
            case "paying" -> payment.getUserId();
            default -> null;
        };
    }
}
*/
