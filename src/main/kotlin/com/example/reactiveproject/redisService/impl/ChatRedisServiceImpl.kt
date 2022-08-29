package com.example.reactiveproject.redisService.impl

import com.example.reactiveproject.Services
import com.example.reactiveproject.helper.*
import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.FullChat
import com.example.reactiveproject.redisService.ChatRedisService
import com.example.reactiveproject.repository.FullChatRepository
import com.example.reactiveproject.repository.UserRepository
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.data.redis.core.ReactiveHashOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.annotation.PostConstruct

@Service
class ChatRedisServiceImpl(val fullChatRepository: FullChatRepository): ChatRedisService{
    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate
    @Autowired
    private lateinit var reactiveRedisTemplateChat: ReactiveRedisTemplate<String, Services.ChatResponse>
    @Autowired
    private lateinit var reactiveRedisTemplateMessage: ReactiveRedisTemplate<String, Services.MessageResponse>
    @Autowired
    private lateinit var reactiveRedisTemplate: ReactiveRedisTemplate<String, Services.UserResponse>


    private lateinit var reactiveHashOps: ReactiveHashOperations<String, String, Chat>



    val logger: Logger = LoggerFactory.getLogger(UserRepository::class.java)

    //упаковываем возвращенные хеш-операции из RedisTemplate экземпляра в HashOperations интерфейс
    @PostConstruct
    fun setUp(){
        reactiveHashOps = redisTemplate.opsForHash()
    }

    override fun createChat(chat: Chat): Mono<Chat> {
        return reactiveRedisTemplateChat.opsForValue().set(chat.id!!, chatToGrpc(chat)).map { chat }

    }

    override fun deleteChat(id: String): Mono<Void> {
        return redisTemplate.opsForValue().delete(id).then()

    }

    override fun addUserToTheChat(chatId: String, userId: String): Mono<Chat> {
        return grpcToChatMono(reactiveRedisTemplateChat.opsForValue().get(chatId))
            .flatMap {
                reactiveRedisTemplateChat.opsForValue().getAndSet(chatId, chatToGrpc(it.apply {
                    userIds!!.add(userId)
                }))
            }.map {
                grpcToChatUnMono(it)
            }
    }

    override fun deleteUserFromChat(chatId: String, userId: String): Mono<Chat> {
        return grpcToChatMono(reactiveRedisTemplateChat.opsForValue().get(chatId))
            .flatMap {
                reactiveRedisTemplateChat.opsForValue().getAndSet(chatId, chatToGrpc(it.apply {
                    userIds!!.remove(userId)
                }))
            }.map {
                grpcToChatUnMono(it)
            }
    }

}