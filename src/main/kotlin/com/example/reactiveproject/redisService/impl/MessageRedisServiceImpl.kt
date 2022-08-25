package com.example.reactiveproject.redisService.impl

import com.example.reactiveproject.Services
import com.example.reactiveproject.helper.messageToGrpcUnMono
import com.example.reactiveproject.helper.monoToFlux
import com.example.reactiveproject.model.Message
import com.example.reactiveproject.redisService.MessageRedisService
import com.example.reactiveproject.repository.UserRepository
import com.example.reactiveproject.service.MessageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveHashOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct


@Service
class MessageRedisServiceImpl: MessageRedisService
{
    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate
    @Autowired
    private lateinit var reactiveRedisTemplateMessage: ReactiveRedisTemplate<String, Services.MessageResponse>

    private lateinit var reactiveHashOps: ReactiveHashOperations<String, String, Message>

    val logger: Logger = LoggerFactory.getLogger(UserRepository::class.java)

    //упаковываем возвращенные хеш-операции из RedisTemplate экземпляра в HashOperations интерфейс
    @PostConstruct
    fun setUp(){
        reactiveHashOps = redisTemplate.opsForHash()
    }

    override fun sendMessage(message: Message?): Mono<Message> {
        return reactiveRedisTemplateMessage.opsForValue()
            .set(message!!.id!!, messageToGrpcUnMono(message))
            .map { message }
            .doOnError{it.printStackTrace()}
            .doOnSuccess {
                logger.info(String.format("Message is CREATED. To find this message, use ID: ${message.id}"))
            }
    }

    override fun deleteMessage(messageId: String): Mono<Void> {
        return redisTemplate.opsForValue()
            .delete(messageId)
            .then()
            .doOnSuccess {
                logger.info(String.format("Message with ID: $messageId is DELETED"))
            }
    }

    override fun editMessage(id: String, message: Message): Mono<Message> {
        return reactiveRedisTemplateMessage.opsForValue()
            .getAndSet(message.id!!, messageToGrpcUnMono(message))
            .map { message }
    }
}
