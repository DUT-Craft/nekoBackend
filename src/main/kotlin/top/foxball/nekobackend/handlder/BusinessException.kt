package top.foxball.nekobackend.handlder

import org.springframework.http.HttpStatus

/**
 * 业务异常基类
 * 用于处理业务逻辑中的各种异常情况，封装 HTTP 状态码和错误消息
 * @param status HTTP 状态码
 * @param message 错误消息
 */
open class BusinessException(
    val status: HttpStatus,
    override val message: String
) : RuntimeException(message) {
    /** 异常对应的 HTTP 状态码数值 */
    val code: Int = status.value()
}

// ===================== 具体业务异常类 =====================

/**
 * 用户未找到异常
 * 当查询的用户不存在时抛出
 */
class UserNotFoundException(
    message: String = "用户不存在"
) : BusinessException(HttpStatus.NOT_FOUND, message)

/**
 * 用户已存在异常
 * 当尝试创建已存在的用户时抛出
 */
class UserAlreadyExistsException(
    message: String = "用户已存在"
) : BusinessException(HttpStatus.CONFLICT, message)

/**
 * 用户名或密码错误异常
 * 当登录时用户名或密码不匹配时抛出
 */
class UsernameOrPasswordErrorException(
    message: String = "用户名或密码错误"
) : BusinessException(HttpStatus.UNAUTHORIZED, message)

/**
 * 用户被禁用异常
 * 当用户账号状态为禁用时抛出
 */
class UserDisabledException(
    message: String = "用户被禁用"
) : BusinessException(HttpStatus.FORBIDDEN, message)

/**
 * 未授权异常
 * 当用户未提供有效的认证信息时抛出
 */
class UnauthorizedException(
    message: String = "未授权"
) : BusinessException(HttpStatus.UNAUTHORIZED, message)

/**
 * 禁止访问异常
 * 当用户没有权限访问特定资源时抛出
 */
class ForbiddenException(
    message: String = "禁止访问"
) : BusinessException(HttpStatus.FORBIDDEN, message)

/**
 * 资源未找到异常
 * 当请求的资源不存在时抛出
 */
class ResourceNotFoundException(
    message: String = "资源不存在"
) : BusinessException(HttpStatus.NOT_FOUND, message)

/**
 * 参数错误异常
 * 当请求参数格式不正确或缺少必要参数时抛出
 */
class ParamErrorException(
    message: String = "参数错误"
) : BusinessException(HttpStatus.BAD_REQUEST, message)

/**
 * Token 无效异常
 * 当提供的 Token 无法识别或无效时抛出
 */
class TokenInvalidException(
    message: String = "Token 无效"
) : BusinessException(HttpStatus.UNAUTHORIZED, message)

/**
 * Token 过期异常
 * 当 Token 已超过有效期时抛出
 */
class TokenExpiredException(
    message: String = "Token 过期"
) : BusinessException(HttpStatus.UNAUTHORIZED, message)

class TokenForbiddenException(
    message: String = "Token 校验失败"
) : BusinessException(HttpStatus.FORBIDDEN, message)
