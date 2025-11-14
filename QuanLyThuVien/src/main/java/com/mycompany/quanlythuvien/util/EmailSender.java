package com.mycompany.quanlythuvien.util;

import java.io.InputStream;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailSender {
    private Properties props;
    private final String user;
    private final String password;
    private Session session;

    public EmailSender() {
        this.props = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
            
            if (input == null) {
                System.out.println("Xin lỗi, không tìm thấy file mail.properties");
                throw new RuntimeException("Không tìm thấy file mail.properties");
            }

            props.load(input);

            this.user = props.getProperty("mail.smtp.user");
            this.password = props.getProperty("mail.smtp.password");

            this.session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tải cấu hình email", e);
        }
    }

    public void sendEmail(String toEmail, String subject, String body) {
        
        if (this.session == null) {
            System.err.println("Session chưa được khởi tạo!");
            return;
        }

        try {
            // Tạo đối tượng MimeMessage (dùng Session đã tạo sẵn)
            MimeMessage message = new MimeMessage(session);
            
            // Người gửi (chính là tài khoản đã cấu hình)
            message.setFrom(new InternetAddress(this.user));

            // Người nhận
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // Chủ đề
            message.setSubject(subject);

            // Nội dung
            message.setText(body);

            // Gửi tin nhắn
            Transport.send(message);

            System.out.println("Gửi email thành công tới " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Gửi email thất bại: " + e.getMessage());
        }
    }
}
