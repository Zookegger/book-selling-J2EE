package com.group.book_selling.template;

/**
 * Helper class chứa các template email HTML.
 */
public class EmailTemplate {

    /**
     * Template email cho xác minh email.
     * Trả về chuỗi HTML với nội dung cá nhân hóa và liên kết xác minh.
     *
     * @param name             Tên của người dùng
     * @param verificationLink URL đầy đủ để xác minh email
     * @return Template email HTML dưới dạng chuỗi
     */
    public static String verificationEmailTemplate(String name, String verificationLink) {
        return "<!doctype html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\" />"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }"
                + ".header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }"
                + ".content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }"
                + ".button { display: inline-block; background-color: #3498db; color: white !important; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; font-weight: bold; }"
                + ".button:hover { background-color: #2980b9; }"
                + ".button#verify { align-self: center; }"
                + ".footer { text-align: center; font-size: 12px; color: #999; margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; }"
                + ".link-text { word-break: break-all; font-size: large; color: #3498db; }"
                + "p { font-size: large; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<div class=\"header\">"
                + "<h1>Chào mừng đến với Lumina Book</h1>"
                + "</div>"
                + "<div class=\"content\">"
                + "<h2>Xác thực email</h2>"
                + "<p>Xin chào " + (name == null || name.isBlank() ? "bạn" : name) + ",</p>"
                + "<p>Cảm ơn bạn đã đăng ký tài khoản tại Lumina Book. Vui lòng xác thực email để hoàn tất đăng ký và bắt đầu khám phá các đầu sách của chúng tôi.</p>"
                + "<div style=\"display: flex; flex-direction: column\">"
                + "<a href=\"" + verificationLink + "\" class=\"button\" id=\"verify\">Xác thực email</a>"
                + "</div>"
                + "<p>Or copy and paste this link in your browser:</p>"
                + "<p class=\"link-text\">" + verificationLink + "</p>"
                + "<p>  Liên kết xác minh này sẽ hết hạn sau 1 giờ..</p>"
                + "<p>Nếu bạn không tạo tài khoản tại Lumina Book, vui lòng bỏ qua email này.</p>"
                + "</div>"
                + "<div class=\"footer\">"
                + "<p>© 2026 Lumina Book. All rights reserved.</p>"
                + "<p>This is an automated email. Please do not reply to this address.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    public static String passwordResetEmailTemplate(String name, String resetLink) {
        return "<!doctype html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\" />"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }"
                + ".header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }"
                + ".content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }"
                + ".button { display: inline-block; background-color: #e74c3c; color: white !important; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; font-weight: bold; }"
                + ".button:hover { background-color: #c0392b; }"
                + ".footer { text-align: center; font-size: 12px; color: #999; margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; }"
                + ".link-text { word-break: break-all; font-size: large; color: #e74c3c; }"
                + "p { font-size: large; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<div class=\"header\">"
                + "<h1>Yêu cầu đặt lại mật khẩu</h1>"
                + "</div>"
                + "<div class=\"content\">"
                + "<h2>Xin chào " + (name == null || name.isBlank() ? "bạn" : name) + ",</h2>"
                + "<p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>"
                + "<p>Nhấp vào nút dưới đây để tạo mật khẩu mới:</p>"
                + "<div style=\"display: flex; flex-direction: column\">"
                + "<a href=\"" + resetLink + "\" class=\"button\">Đặt lại mật khẩu</a>"
                + "</div>"
                + "<p>Hoặc sao chép và dán liên kết này vào trình duyệt của bạn:</p>"
                + "<p class=\"link-text\">" + resetLink + "</p>"
                + "<p>Liên kết này sẽ hết hạn sau 1 giờ.</p>"
                + "<p>Nếu bạn không gửi yêu cầu này, vui lòng bỏ qua email.</p>"
                + "</div>"
                + "<div class=\"footer\">"
                + "<p>© 2026 Lumina Book. All rights reserved.</p>"
                + "<p>This is an automated email. Please do not reply to this address.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
}
