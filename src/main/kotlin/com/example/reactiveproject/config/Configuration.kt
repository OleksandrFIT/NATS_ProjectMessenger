package com.example.reactiveproject.config

import io.nats.client.Connection
import io.nats.client.Nats
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class Configuration {
    @Bean
    fun initializationNatsConnection(): Connection {
        val url = "nats://localhost:4222"
        return Nats.connect(url)
    }
}