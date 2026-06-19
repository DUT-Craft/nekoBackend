package top.foxball.nekobackend.security.permission

import java.lang.annotation.Inherited

/** 方法级用户组校验注解。 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireRole(
    /** 需要属于的用户组编码，支持填写 ADMIN 或 ROLE_ADMIN。 */
    vararg val value: String,

    /** 多个用户组编码之间的匹配模式，默认必须全部满足。 */
    val mode: AuthMatchMode = AuthMatchMode.ALL,
)
