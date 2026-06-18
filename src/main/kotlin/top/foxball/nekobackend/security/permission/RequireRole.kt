package top.foxball.nekobackend.security.permission

import java.lang.annotation.Inherited

/**
 * 方法级角色校验注解。
 *
 * 支持填写 ADMIN 或 ROLE_ADMIN，内部会统一规范化为 Spring Security 的 ROLE_ 前缀格式。
 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireRole(
    /** 需要拥有的角色编码。 */
    vararg val value: String,

    /** 多个角色编码之间的匹配模式，默认必须全部满足。 */
    val mode: AuthMatchMode = AuthMatchMode.ALL,
)
