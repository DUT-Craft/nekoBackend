package top.foxball.nekobackend.security.permission

import org.springframework.aop.support.ComposablePointcut
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor

/** 注册自定义权限注解的方法级授权拦截器。 */
@Configuration
class PermissionMethodSecurityConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun permissionAuthorizationMethodInterceptor(
        authorizationManager: PermissionAuthorizationManager,
    ): AuthorizationManagerBeforeMethodInterceptor {
        val pointcut = ComposablePointcut(AnnotationMatchingPointcut.forClassAnnotation(RequirePermission::class.java))
            .union(AnnotationMatchingPointcut.forMethodAnnotation(RequirePermission::class.java))
            .union(AnnotationMatchingPointcut.forClassAnnotation(RequireRole::class.java))
            .union(AnnotationMatchingPointcut.forMethodAnnotation(RequireRole::class.java))

        val interceptor = AuthorizationManagerBeforeMethodInterceptor(pointcut, authorizationManager)
        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.order + 1)
        return interceptor
    }
}
