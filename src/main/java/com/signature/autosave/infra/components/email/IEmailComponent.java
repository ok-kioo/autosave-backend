package com.signature.autosave.infra.components.email;

import jakarta.mail.MessagingException;

import java.util.List;

public interface IEmailComponent {
    void sendEmail(String to, String subject, String html) throws MessagingException;
    void sendEmail(List<String> bcc, String subject, String html) throws MessagingException;
}
