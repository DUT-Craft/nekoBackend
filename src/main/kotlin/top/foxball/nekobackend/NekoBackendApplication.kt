package top.foxball.nekobackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NekoBackendApplication

fun main(args: Array<String>) {
    runApplication<NekoBackendApplication>(*args)
}
