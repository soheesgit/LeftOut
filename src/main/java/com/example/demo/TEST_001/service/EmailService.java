package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.NotificationDTO;
import com.example.demo.TEST_001.dto.UserDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * 비동기 이메일 발송 (유통기한 알림용)
     */
    @Async
    public void sendExpiryNotificationEmail(UserDTO user, NotificationDTO notification) {
        // 이메일 설정 체크
        if (fromEmail == null || fromEmail.isBlank()) {
            log.debug("이메일 발송 설정이 되어있지 않습니다.");
            return;
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.debug("사용자 이메일 미등록: userId={}", user.getId());
            return;
        }

        if (!Boolean.TRUE.equals(user.getEmailNotificationEnabled())) {
            log.debug("사용자 이메일 알림 비활성화: userId={}", user.getId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "냉털이 LeftOut");
            helper.setTo(user.getEmail());
            helper.setSubject(buildSubject(notification));
            helper.setText(buildHtmlContent(user, notification), true);

            mailSender.send(message);
            log.info("이메일 발송 성공: userId={}, email={}, type={}",
                    user.getId(), user.getEmail(), notification.getType());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("이메일 발송 실패: userId={}, email={}",
                    user.getId(), user.getEmail(), e);
        } catch (Exception e) {
            log.error("이메일 발송 중 예외 발생: userId={}", user.getId(), e);
        }
    }

    /**
     * 이메일 제목 생성
     */
    private String buildSubject(NotificationDTO notification) {
        return switch (notification.getType()) {
            case "EXPIRY_DDAY" -> "[냉털이] 오늘이 유통기한입니다!";
            case "EXPIRY_D1" -> "[냉털이] 내일 유통기한 임박 식재료가 있습니다";
            case "EXPIRY_D3" -> "[냉털이] 유통기한 3일 전 알림";
            default -> "[냉털이] " + notification.getTitle();
        };
    }

    /**
     * HTML 이메일 본문 생성
     */
    private String buildHtmlContent(UserDTO user, NotificationDTO notification) {
        String urgencyColor = switch (notification.getType()) {
            case "EXPIRY_DDAY" -> "#dc3545";  // 빨강
            case "EXPIRY_D1" -> "#fd7e14";    // 주황
            case "EXPIRY_D3" -> "#28a745";    // 초록
            default -> "#6c757d";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <div style="background: linear-gradient(135deg, #2e7d32, #4caf50); padding: 30px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">&#129482; 냉털이</h1>
                        <p style="color: rgba(255,255,255,0.9); margin: 10px 0 0;">유통기한 알림</p>
                    </div>

                    <div style="padding: 30px;">
                        <p style="color: #333; font-size: 16px;">안녕하세요, <strong>%s</strong>님!</p>

                        <div style="background: %s; color: white; padding: 20px; border-radius: 8px; margin: 20px 0;">
                            <h2 style="margin: 0 0 10px; font-size: 18px;">%s</h2>
                            <p style="margin: 0; font-size: 16px;">%s</p>
                        </div>

                        <p style="color: #666; font-size: 14px; line-height: 1.6;">
                            신선한 재료로 맛있는 요리를 만들어보세요!<br>
                            유통기한이 지나기 전에 활용하면 음식물 쓰레기도 줄일 수 있어요.
                        </p>
                    </div>

                    <div style="background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #eee;">
                        <p style="color: #999; font-size: 12px; margin: 0;">
                            이 메일은 냉털이 유통기한 알림 서비스입니다.<br>
                            수신을 원하지 않으시면 설정에서 이메일 알림을 끄시면 됩니다.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(user.getName(), urgencyColor, notification.getTitle(), notification.getMessage());
    }

    /**
     * 테스트용 이메일 발송
     */
    public boolean sendTestEmail(String toEmail) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("이메일 발송 설정이 되어있지 않습니다.");
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "냉털이 LeftOut");
            helper.setTo(toEmail);
            helper.setSubject("[냉털이] 테스트 이메일");
            helper.setText("""
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 500px; margin: 0 auto; background: #e8f5e9; padding: 30px; border-radius: 12px; text-align: center;">
                        <h1 style="color: #2e7d32;">&#129482; 테스트 이메일</h1>
                        <p style="color: #333; font-size: 16px;">이메일 설정이 정상적으로 완료되었습니다!</p>
                        <p style="color: #666; font-size: 14px;">이제 유통기한 알림을 이메일로 받으실 수 있습니다.</p>
                    </div>
                </body>
                </html>
                """, true);

            mailSender.send(message);
            log.info("테스트 이메일 발송 성공: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("테스트 이메일 발송 실패: {}", toEmail, e);
            return false;
        }
    }

    /**
     * 일일 유통기한 알림 통합 이메일 발송 (사용자당 하나의 이메일)
     */
    @Async
    public void sendDailyExpiryDigestEmail(UserDTO user, List<Map<String, Object>> items) {
        // 이메일 설정 체크
        if (fromEmail == null || fromEmail.isBlank()) {
            log.debug("이메일 발송 설정이 되어있지 않습니다.");
            return;
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.debug("사용자 이메일 미등록: userId={}", user.getId());
            return;
        }

        if (!Boolean.TRUE.equals(user.getEmailNotificationEnabled())) {
            log.debug("사용자 이메일 알림 비활성화: userId={}", user.getId());
            return;
        }

        if (items == null || items.isEmpty()) {
            log.debug("알림할 식재료가 없습니다: userId={}", user.getId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "냉털이 LeftOut");
            helper.setTo(user.getEmail());
            helper.setSubject("[냉털이] 오늘의 유통기한 알림");
            helper.setText(buildDigestHtmlContent(user, items), true);

            mailSender.send(message);
            log.info("통합 이메일 발송 성공: userId={}, email={}, 식재료수={}",
                    user.getId(), user.getEmail(), items.size());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("통합 이메일 발송 실패: userId={}, email={}",
                    user.getId(), user.getEmail(), e);
        } catch (Exception e) {
            log.error("통합 이메일 발송 중 예외 발생: userId={}", user.getId(), e);
        }
    }

    /**
     * 통합 이메일 HTML 본문 생성
     */
    private String buildDigestHtmlContent(UserDTO user, List<Map<String, Object>> items) {
        // D-Day, D-1, D-3로 분류
        List<String> dDayItems = new ArrayList<>();
        List<String> d1Items = new ArrayList<>();
        List<String> d3Items = new ArrayList<>();

        for (Map<String, Object> item : items) {
            String ingredientName = (String) item.get("ingredientName");
            Integer daysUntilExpiry = ((Number) item.get("daysUntilExpiry")).intValue();

            switch (daysUntilExpiry) {
                case 0 -> dDayItems.add(ingredientName);
                case 1 -> d1Items.add(ingredientName);
                case 3 -> d3Items.add(ingredientName);
            }
        }

        StringBuilder sectionsHtml = new StringBuilder();

        // D-Day 섹션 (빨강)
        if (!dDayItems.isEmpty()) {
            sectionsHtml.append(buildSection("🔴 오늘이 유통기한!", "#dc3545", dDayItems));
        }

        // D-1 섹션 (주황)
        if (!d1Items.isEmpty()) {
            sectionsHtml.append(buildSection("🟠 내일이 유통기한!", "#fd7e14", d1Items));
        }

        // D-3 섹션 (초록)
        if (!d3Items.isEmpty()) {
            sectionsHtml.append(buildSection("🟢 3일 남았어요", "#28a745", d3Items));
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <div style="background: linear-gradient(135deg, #2e7d32, #4caf50); padding: 30px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">🧊 냉털이</h1>
                        <p style="color: rgba(255,255,255,0.9); margin: 10px 0 0;">오늘의 유통기한 알림</p>
                    </div>

                    <div style="padding: 30px;">
                        <p style="color: #333; font-size: 16px;">안녕하세요, <strong>%s</strong>님!</p>
                        <p style="color: #666; font-size: 14px; margin-bottom: 20px;">유통기한이 임박한 식재료가 있어요.</p>

                        %s

                        <p style="color: #666; font-size: 14px; line-height: 1.6; margin-top: 20px;">
                            신선한 재료로 맛있는 요리를 만들어보세요!<br>
                            유통기한이 지나기 전에 활용하면 음식물 쓰레기도 줄일 수 있어요.
                        </p>
                    </div>

                    <div style="background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #eee;">
                        <p style="color: #999; font-size: 12px; margin: 0;">
                            이 메일은 냉털이 유통기한 알림 서비스입니다.<br>
                            수신을 원하지 않으시면 설정에서 이메일 알림을 끄시면 됩니다.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(user.getName(), sectionsHtml.toString());
    }

    /**
     * 각 섹션(D-Day, D-1, D-3) HTML 생성
     */
    private String buildSection(String title, String color, List<String> items) {
        StringBuilder itemsHtml = new StringBuilder();
        for (String item : items) {
            itemsHtml.append("<li style=\"margin: 5px 0;\">").append(item).append("</li>");
        }

        return """
            <div style="background: %s; color: white; padding: 15px 20px; border-radius: 8px; margin: 10px 0;">
                <h3 style="margin: 0 0 10px; font-size: 16px;">%s</h3>
                <ul style="margin: 0; padding-left: 20px;">
                    %s
                </ul>
            </div>
            """.formatted(color, title, itemsHtml.toString());
    }
}
