package com.example.reactiveproject.service.impl

import com.example.reactiveproject.helper.chatToGrpc
import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.FullChat
import com.example.reactiveproject.model.User
import org.springframework.beans.factory.annotation.Autowired
import com.example.reactiveproject.repository.ChatRepository
import com.example.reactiveproject.repository.FullChatRepository
import com.example.reactiveproject.repository.MessageRepository
import com.example.reactiveproject.repository.UserRepository
import com.example.reactiveproject.service.ChatService
import io.nats.client.Connection
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono


@Service
class ChatServiceImpl(
    val chatRepository: ChatRepository,
    val userRepository: UserRepository?,
    val fullChatRepository: FullChatRepository,
    val messageRepository: MessageRepository?
) : ChatService {

    @Autowired
    lateinit var natsConnector: Connection

    override fun createChat(chat: Chat): Mono<Chat>{
        return chatRepository.save(chat).doOnSuccess {
            natsConnector.publish("chat-event", chatToGrpc(it).toByteArray())
        }
    }

    override fun deleteChat(id: String): Mono<Void> {
        return chatRepository.deleteById(id)
    }

    override fun addUserToTheChat(chatId: String, userId: String):Mono<Chat> {

        return userRepository!!.findById(userId)
            .switchIfEmpty(
                Mono.error(NotFoundException())
            )
            .then(
                chatRepository.findChatById(chatId)
            )
            .map {
                it!!.userIds
                Chat(
                    id = chatId,
                    name = it.name,
                    messageIds = it.messageIds,
                    userIds = it.userIds!!.plus(userId) as HashSet
                )
            }
            .flatMap {
                chatRepository.save(it)
            }
            .toMono()
    }


    override fun deleteUserFromChat(chatId: String, userId: String): Mono<Chat> {

        return userRepository!!.findById(userId)
            .switchIfEmpty(
                Mono.error(NotFoundException())
            )
            .then(
                chatRepository.findChatById(chatId)
            )
            .map {
                it!!.userIds
                Chat(
                    id = chatId,
                    name = it.name,
                    messageIds = it.messageIds,
                    userIds = it.userIds!!.minus(userId) as HashSet
                )
            }
            .flatMap {
                chatRepository.save(it)
            }
            .toMono()
    }


    override fun findAllChats(): Flux<Chat> {
        return chatRepository.findAll()
    }

    override fun getChatById(chatId: String): Mono<FullChat> {

        val userF = chatRepository.findChatById(chatId)
            .switchIfEmpty(
                Mono.error(NotFoundException())
            )
            .flatMapIterable {
                it!!.userIds!!
            }
            .flatMap {
                userRepository!!.findById(it)
            }
            .collectList()

        val messageF = chatRepository.findChatById(chatId)
            .switchIfEmpty(
                Mono.error(NotFoundException())
            )
            .flatMapIterable {
                it!!.messageIds!!
            }
            .flatMap {
                messageRepository!!.findById(it)
            }
            .collectList()

        val chat = chatRepository.findChatById(chatId)

        return Mono.zip(chat, userF, messageF)
            .flatMap {
                fullChatRepository.save(FullChat(
                    chat = it.t1,
                    userList = it.t2,
                    messageList = it.t3
                ))
            }



    }


}
