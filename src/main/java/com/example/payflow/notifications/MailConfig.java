package com.example.payflow.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host:smtp.gmail.com}") String host,
            @Value("${spring.mail.port:587}") int port,
            @Value("${spring.mail.username:basitmohammed300@gmail.com}") String username,
            @Value("${spring.mail.password:jpfy owkp qdmc mxyb}") String password,
            @Value("${spring.mail.properties.mail.smtp.auth:true}") boolean smtpAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") boolean startTlsEnable,
            @Value("${spring.mail.properties.mail.smtp.starttls.required:true}") boolean startTlsRequired) {
        var sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", smtpAuth);
        properties.put("mail.smtp.starttls.enable", startTlsEnable);
        properties.put("mail.smtp.starttls.required", startTlsRequired);

        return sender;
    }
}
