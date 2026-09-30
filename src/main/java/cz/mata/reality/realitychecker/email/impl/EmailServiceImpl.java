package cz.mata.reality.realitychecker.email.impl;

import cz.mata.reality.realitychecker.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/*
 * @created 09/11/2021 - 22:16
 * @project RealityChecker
 * @author msejkora
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String NOREPLY_ADDRESS = "sejk.m@seznam.cz";

    private final JavaMailSender emailSender;

    @Override
    public void sendSimpleMessage(String subject, String text, String... to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(NOREPLY_ADDRESS);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            emailSender.send(message);
        } catch (MailException exception) {
            log.error("EmailService exception {}", exception.getStackTrace());
        }
    }
}
