package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.service.UserService
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder

@RestController
@RequestMapping("/api/")
class UserController(
    private val userService: UserService,
    private val builder: ResponseBuilder
) {
    @GetMapping("userByUsername")
    fun getUserByUsername(
        @RequestParam username: String
    ) : ResponseEntity<Response>{
        val user = userService.findByUsername(username)

        data class Response(
            val username: String,
            val nickname: String,
            val avatar: String,
            val signature: String
        )

        val rs = user?.let {
            Response(
                username = it.username!!,
                nickname = user.nickname!!,
                avatar = user.avatar!!,
                signature = user.signature!!
            )
        }

        return builder.ok().data(rs).build()
    }
}