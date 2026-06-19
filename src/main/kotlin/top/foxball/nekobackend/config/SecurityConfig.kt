package top.foxball.nekobackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import top.foxball.nekobackend.security.jwt.JwtAuthenticationFilter
import top.foxball.nekobackend.security.RestAccessDeniedHandler
import top.foxball.nekobackend.security.RestAuthenticationEntryPoint

/**
 * Spring Security 安全配置。
 *
 * 这里负责把 JWT 过滤器、认证失败响应和授权失败响应接入过滤器链。
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
    private val restAccessDeniedHandler: RestAccessDeniedHandler,
) {

    /**
     * 构建无状态安全过滤器链。
     *
     * 登录、注册和注册验证码接口放行，其余接口默认要求认证。
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .exceptionHandling {
                it.authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/email-code/register").permitAll()
                    .requestMatchers("/file/**").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
