package top.foxball.nekobackend.security.permission

import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.support.AopUtils
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.authorization.AuthorizationResult
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.function.Supplier

/** 把 @RequirePermission 和 @RequireRole 接入 Spring Security 方法级授权。 */
@Component
class PermissionAuthorizationManager(
    private val authorizationService: PermissionAuthorizationService,
) : AuthorizationManager<MethodInvocation> {

    override fun authorize(
        authentication: Supplier<out Authentication>,
        invocation: MethodInvocation,
    ): AuthorizationResult {
        val targetClass = invocation.getThis()?.let { AopUtils.getTargetClass(it) }
            ?: invocation.method.declaringClass
        val method = AopUtils.getMostSpecificMethod(invocation.method, targetClass)

        val permissionAnnotations = findAnnotations<RequirePermission>(targetClass, method, invocation)
        val roleAnnotations = findAnnotations<RequireRole>(targetClass, method, invocation)

        if (permissionAnnotations.isEmpty() && roleAnnotations.isEmpty()) {
            return AuthorizationDecision(true)
        }

        val currentAuthentication = authentication.get()
        val permissionGranted = permissionAnnotations.all {
            authorizationService.hasPermissions(currentAuthentication, it.value, it.mode)
        }
        val roleGranted = roleAnnotations.all {
            authorizationService.hasRoles(currentAuthentication, it.value, it.mode)
        }

        return AuthorizationDecision(permissionGranted && roleGranted)
    }

    private inline fun <reified A : Annotation> findAnnotations(
        targetClass: Class<*>,
        method: java.lang.reflect.Method,
        invocation: MethodInvocation,
    ): List<A> {
        val classAnnotation = AnnotatedElementUtils.findMergedAnnotation(targetClass, A::class.java)
            ?: AnnotatedElementUtils.findMergedAnnotation(invocation.method.declaringClass, A::class.java)
        val methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, A::class.java)
            ?: AnnotatedElementUtils.findMergedAnnotation(invocation.method, A::class.java)

        return listOfNotNull(classAnnotation, methodAnnotation)
    }
}
