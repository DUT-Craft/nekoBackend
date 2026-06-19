package top.foxball.nekobackend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "neko.mail.verification")
class EmailVerificationProperties {
    /** 邮件验证码有效期，单位秒。 */
    var ttlSeconds: Long = 600

    /** 验证码位数。 */
    var codeLength: Int = 6

    /** 发件人地址，为空时由 JavaMailSender 使用默认账号。 */
    var from: String = ""

    /** 验证码邮件标题前缀。 */
    var subjectPrefix: String = "NekoBackend"
}
