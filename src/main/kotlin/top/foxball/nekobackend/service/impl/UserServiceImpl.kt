package top.foxball.nekobackend.service.impl

import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.User
import top.foxball.nekobackend.datasource.jdbc.UserRepository
import top.foxball.nekobackend.service.UserService

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
): UserService {

    override fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

     override fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

     override fun save(user: User): User {
        return userRepository.save(user)
    }

     override fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }
}
