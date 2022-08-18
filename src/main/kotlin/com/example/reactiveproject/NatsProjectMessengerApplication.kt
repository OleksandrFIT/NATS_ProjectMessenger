package com.example.reactiveproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate


@SpringBootApplication
class NatsProjectMessengerApplication

fun main(args: Array<String>) {
    runApplication<NatsProjectMessengerApplication>(*args)
}
