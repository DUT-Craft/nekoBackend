package top.foxball.nekobackend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/** 文件上传下载配置。 */
@Component
@ConfigurationProperties(prefix = "neko.file")
class FileStorageProperties {
    /** 文件物理存储根路径。 */
    var storagePath: String = "./storage"

    /** 返回给前端的访问域名或完整基础 URL。 */
    var baseUrl: String = "http://localhost:8080"
}
