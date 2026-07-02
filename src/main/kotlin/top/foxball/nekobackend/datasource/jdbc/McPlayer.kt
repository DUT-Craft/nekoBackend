package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*

@Entity
@Table(
    name = "mc_player",
    indexes = [
        Index(name = "idx_mc_player_user_id", columnList = "user_id"),
        Index(name = "idx_mc_player_server_id", columnList = "minecraft_java_server_id"),
        Index(name = "idx_mc_player_uuid", columnList = "uuid"),
        Index(name = "idx_mc_player_name", columnList = "name"),
        Index(name = "idx_mc_player_status", columnList = "status"),
    ],
)
class McPlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "user_id")
    var userId: Long? = null

    @Column(name = "uuid", length = 36)
    var uuid: String? = null

    @Column(name = "display_name", length = 64)
    var displayName: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: McPlayerStatus? = McPlayerStatus.UNKNOWN

    /**
     * 近 30 天每日在线时长统计，JSON 字符串。
     *
     * 建议格式：
     * [
     *   {"date":"2026-06-21","onlineSeconds":3600}
     * ]
     */
    @Column(name = "daily_online_time_stats", columnDefinition = "text")
    var dailyOnlineTimeStats: String? = null

    /**
     * 近 7 天每小时在线时长统计，JSON 字符串。
     *
     * 建议格式：
     * [
     *   {"hour":"2026-06-21T13:00:00","onlineSeconds":600}
     * ]
     */
    @Column(name = "hourly_online_time_stats", columnDefinition = "text")
    var hourlyOnlineTimeStats: String? = null

}

enum class McPlayerStatus {
    UNKNOWN,
    ONLINE,
    OFFLINE,
    BANNED,
    WHITELISTED,
}
