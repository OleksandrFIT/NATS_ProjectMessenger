package com.example.reactiveproject.config

import ProtobufSerializer
import com.example.reactiveproject.Services
import com.example.reactiveproject.model.Message
import com.example.reactiveproject.model.User
import io.nats.client.Connection
import io.nats.client.Nats
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component

@Component
class Configuration {
    @Bean
    fun initializationNatsConnection(): Connection {
        val url = "nats://localhost:4222"
        return Nats.connect(url)
    }

    @Bean
    @Primary
    fun connectionFactory(): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory("localhost", 6379)
    }

    @Bean
    fun reactiveRedisTemplate(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Services.UserResponse> {
        val keySerializer: StringRedisSerializer = StringRedisSerializer()

        val valueSerializer: ProtobufSerializer<Services.UserResponse> =
            ProtobufSerializer(Services.UserResponse::class.java)

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, Services.UserResponse> =
            RedisSerializationContext.newSerializationContext(keySerializer)

        val context: RedisSerializationContext<String, Services.UserResponse> = builder.value(valueSerializer).build()

        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, context)
    }

    @Bean
    fun reactiveRedisTemplateMessage(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Services.MessageResponse> {
        val keySerializer: StringRedisSerializer = StringRedisSerializer()

        val valueSerializer: ProtobufSerializer<Services.MessageResponse> =
            ProtobufSerializer(Services.MessageResponse::class.java)

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, Services.MessageResponse> =
            RedisSerializationContext.newSerializationContext(keySerializer)

        val context: RedisSerializationContext<String, Services.MessageResponse> = builder.value(valueSerializer).build()

        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, context)
    }

    @Bean
    fun reactiveRedisTemplateChat(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Services.ChatResponse> {
        val keySerializer: StringRedisSerializer = StringRedisSerializer()

        val valueSerializer: ProtobufSerializer<Services.ChatResponse> =
            ProtobufSerializer(Services.ChatResponse::class.java)

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, Services.ChatResponse> =
            RedisSerializationContext.newSerializationContext(keySerializer)

        val context: RedisSerializationContext<String, Services.ChatResponse> = builder.value(valueSerializer).build()

        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, context)
    }
    @Bean
    fun reactiveRedisTemplateChatFullChat(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Services.FullChatResponse> {
        val keySerializer: StringRedisSerializer = StringRedisSerializer()

        val valueSerializer: ProtobufSerializer<Services.FullChatResponse> =
            ProtobufSerializer(Services.FullChatResponse::class.java)

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, Services.FullChatResponse> =
            RedisSerializationContext.newSerializationContext(keySerializer)

        val context: RedisSerializationContext<String, Services.FullChatResponse> = builder.value(valueSerializer).build()

        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, context)
    }
}