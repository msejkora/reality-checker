package cz.mata.reality.realitychecker.email;

/*
 * @created 09/11/2021 - 22:15
 * @project RealityChecker
 * @author msejkora
 */
public interface EmailService {
    void sendSimpleMessage(String subject, String text, String... to);
}
