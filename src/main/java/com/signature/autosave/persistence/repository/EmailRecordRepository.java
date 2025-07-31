/*
package com.signature.autosave.persistence.repository;

import com.signature.autosave.persistence.builder.EmailRecordBuilder;
import com.signature.autosave.persistence.entity.EmailContent;
import com.signature.autosave.persistence.entity.EmailRecord;
import com.signature.autosave.persistence.jpa.JPAManager;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;
import java.util.logging.Logger;

public class EmailRecordRepository implements Repository{
    @Getter
    private static final EmailRecordRepository instance = new EmailRecordRepository();
    private final EntityManager entityManager;
    private static final Logger LOGGER = Logger.getLogger(EmailRecordRepository.class.getName());

    private EmailRecordRepository() {
        this.entityManager = JPAManager.getInstance().getEntityManager();
    }

    @Override
    public Boolean create(Object[] params) {
        UUID emailContentId = UUID.fromString((String) params[0]);

        EmailContent emailId = entityManager.find(EmailContent.class, emailContentId);
        if (emailId == null) {
            throw new IllegalArgumentException("Email not found with id: " + emailContentId);
        }
        EmailRecord emailRecord = EmailRecordBuilder.builder()
                .withEmailContent(emailId)
                .build();

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(emailRecord);
            transaction.commit();
            LOGGER.info("EmailRecord made successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - made emailRecord: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return true;
    }

    @Override
    public Object list(Object[] params) {
        TypedQuery<EmailRecord> query = entityManager.createQuery(
                "SELECT u FROM EmailRecord u", EmailRecord.class);
        try {
            EmailRecord emailRecord = query.getSingleResult();
            return emailRecord.getId();
        } catch (NoResultException e) {
            LOGGER.warning("EmailRecords not found");
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

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            Query query = entityManager.createQuery(
                    "UPDATE EmailRecord e SET e.viewCount = e.viewCount + 1 WHERE e.id = :id"
            );
            query.setParameter("id", id);
            int updatedCount = query.executeUpdate();

            transaction.commit();
            LOGGER.info("EmailRecord updated successfully, updated count: " + updatedCount);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - update emailRecord: " + e.getMessage());
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

            EmailRecord emailRecord = entityManager.find(EmailRecord.class, id);

            entityManager.remove(emailRecord);
            transaction.commit();
            LOGGER.info("EmailRecord deleted successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - delete emailRecord: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return true;
    }

    @Override
    public Object findById(Object[] params) {
        UUID id = UUID.fromString((String) params[0]);
        TypedQuery<EmailRecord> query = entityManager.createQuery(
                "SELECT u FROM EmailRecord u WHERE u.id = :id", EmailRecord.class);
        query.setParameter("id", id);
        try {
            EmailRecord emailRecord = query.getSingleResult();
            return emailRecord.getId();
        } catch (NoResultException e) {
            LOGGER.warning("EmailRecords not found with id: " + id);
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return null;
    }

    @Override
    public UUID findByEmail(String email) {
        return null;
    }

    @Override
    public Object getData(UUID id, String dataToGet) {
        EmailRecord emailRecord = entityManager.find(EmailRecord.class, id);
        if (emailRecord == null) {
            return null;
        }
        return switch (dataToGet) {
            case "viewCount" -> emailRecord.getViewCount();
            case "emailContent" -> emailRecord.getEmailContent();
            default -> null;
        };
    }
}
*/
