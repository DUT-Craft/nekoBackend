package top.foxball.nekobackend.security.permission

import java.lang.annotation.Inherited

/**
 * 方法级权限校验注解。
 *
 * 可标记在 Controller/Service 的类或方法上，权限码必须和数据库 permission.code 保持一致。
 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequirePermission(
    /** 需要拥有的权限码。 */
    vararg val value: String,

    /** 多个权限码之间的匹配模式，默认必须全部满足。 */
    val mode: AuthMatchMode = AuthMatchMode.ALL,
)
