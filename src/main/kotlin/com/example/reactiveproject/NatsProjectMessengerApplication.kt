package com.example.reactiveproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NatsProjectMessengerApplication

fun main(args: Array<String>) {
    runApplication<NatsProjectMessengerApplication>(*args)
}
