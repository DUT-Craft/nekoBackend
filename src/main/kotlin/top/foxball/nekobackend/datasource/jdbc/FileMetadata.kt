package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/** 文件元数据。 */
@Entity
@Table(name = "file_metadata")
class FileMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    /** 上传用户 ID。 */
    @Column(name = "uploaded_by_user_id")
    var uploadedByUserId: Long? = null,

    /** 用户上传时的原始文件名。 */
    @Column(name = "original_filename", nullable = false, length = 255)
    var originalFilename: String,

    /** 实际保存的文件名。 */
    @Column(name = "filename", nullable = false, length = 255)
    var filename: String,

    /** 文件后缀名。 */
    @Column(name = "extension", length = 32)
    var extension: String? = null,

    /** 文件大小，单位字节。 */
    @Column(name = "file_size", nullable = false)
    var size: Long,

    /** 文件 MIME 类型。 */
    @Column(name = "content_type", length = 128)
    var contentType: String? = null,

    /** 文件物理路径。 */
    @Column(name = "storage_path", nullable = false, length = 1024)
    var storagePath: String,

    /** 公开相对路径。 */
    @Column(name = "relative_path", nullable = false, unique = true, length = 512)
    var relativePath: String,

    /** 返回给前端的下载 URL。 */
    @Column(name = "download_url", nullable = false, length = 1024)
    var downloadUrl: String,

    /** 上传时间。 */
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime? = null,
)
