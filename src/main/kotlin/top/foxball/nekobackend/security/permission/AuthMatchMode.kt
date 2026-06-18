package top.foxball.nekobackend.security.permission

/** 权限或角色的匹配模式。 */
enum class AuthMatchMode {
    /** 满足任意一个要求即可通过。 */
    ANY,

    /** 必须满足全部要求才可通过。 */
    ALL,
}
