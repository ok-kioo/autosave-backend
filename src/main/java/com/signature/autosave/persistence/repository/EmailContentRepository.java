/*
package com.signature.autosave.persistence.repository;

import com.signature.autosave.persistence.builder.EmailContentBuilder;
import com.signature.autosave.persistence.entity.EmailContent;
import com.signature.autosave.persistence.jpa.JPAManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.Getter;

import java.util.UUID;
import java.util.logging.Logger;

public class EmailContentRepository implements Repository{
    @Getter
    private static final EmailContentRepository instance = new EmailContentRepository();
    private final EntityManager entityManager;
    private static final Logger LOGGER = Logger.getLogger(EmailContentRepository.class.getName());

    private EmailContentRepository() {
        this.entityManager = JPAManager.getInstance().getEntityManager();
    }

    @Override
    public Boolean create(Object[] params) {
        String title = (String) params[0];
        String source = (String) params[1];
        String destination = (String) params[2];
        String content = (String) params[3];
        String body = (String) params[4];

        EmailContent emailContent = EmailContentBuilder.builder()
                .withTitle(title)
                .withSource(source)
                .withDestination(destination)
                .withContent(content)
                .withBody(body)
                .build();

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(emailContent);
            transaction.commit();
            LOGGER.info("EmailContent made successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - made emailContent: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return true;
    }

    @Override
    public Object list(Object[] params) {
        TypedQuery<EmailContent> query = entityManager.createQuery(
                "SELECT u FROM EmailContent u", EmailContent.class);
        try {
            EmailContent emailContent = query.getSingleResult();
            return emailContent.getId();
        } catch (NoResultException e) {
            LOGGER.warning("EmailContents not found");
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return null;
    }

    @Override
    public Boolean update(Object[] params) {
        UUID id = (UUID) params[0];
        String title = (String) params[1];
        String source = (String) params[2];
        String destination = (String) params[3];
        String content = (String) params[4];
        String body = (String) params[5];

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            EmailContent emailContent = entityManager.find(EmailContent.class, id);

            emailContent.setTitle(title);
            emailContent.setSource(source);
            emailContent.setDestination(destination);
            emailContent.setContent(content);
            emailContent.setBody(body);

            transaction.commit();
            LOGGER.info("EmailContent updated successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - update emailContent: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return true;
    }

    @Override
    public Boolean delete(Object[] params) {
        UUID id = (UUID) params[0];

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            EmailContent emailContent = entityManager.find(EmailContent.class, id);

            entityManager.remove(emailContent);
            transaction.commit();
            LOGGER.info("EmailContent deleted successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Transaction commit error - delete emailContent: " + e.getMessage());
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
        TypedQuery<EmailContent> query = entityManager.createQuery(
                "SELECT u FROM EmailContent u WHERE u.id = :id", EmailContent.class);
        query.setParameter("id", id);
        try {
            EmailContent emailContent = query.getSingleResult();
            return emailContent.getId();
        } catch (NoResultException e) {
            LOGGER.warning("EmailContent not found with id: " + id);
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
        EmailContent emailContent = entityManager.find(EmailContent.class, id);
        if (emailContent == null) {
            return null;
        }
        return switch (dataToGet) {
            case "title" -> emailContent.getTitle();
            case "source" -> emailContent.getSource();
            case "destination" -> emailContent.getDestination();
            case "content" -> emailContent.getContent();
            case "body" -> emailContent.getBody();
            default -> null;
        };
    }
}
*/
