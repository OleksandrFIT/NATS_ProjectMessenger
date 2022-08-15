package com.example.reactiveproject.controller

import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.FullChat
import com.example.reactiveproject.model.Message
import com.example.reactiveproject.model.User
import com.example.reactiveproject.repository.ChatRepository
import com.example.reactiveproject.repository.MessageRepository
import com.example.reactiveproject.repository.UserRepository
import com.example.reactiveproject.repository.FullChatRepository
import com.example.reactiveproject.service.impl.ChatServiceImpl
import com.example.reactiveproject.service.impl.MessageServiceImpl
import com.example.reactiveproject.service.impl.UserServiceImpl
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.tools.agent.ReactorDebugAgent
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ChatController::class])
@Import(ChatServiceImpl::class, UserServiceImpl::class, MessageServiceImpl::class)
internal class ChatControllerTest{

    @MockBean
    private lateinit var chatRepository: ChatRepository
    @MockBean
    private lateinit var userRepository: UserRepository
    @MockBean
    private lateinit var messageRepository: MessageRepository
    @MockBean
    private lateinit var fullChatRepository: FullChatRepository


    @Autowired
    private lateinit var webClient: WebTestClient

    fun randomName(): String = List(10) {
        (('a'..'z') + ('A'..'Z')).random()
    }.joinToString("")
    private fun randomPhone(): String = List(12) {
        (('0'..'9')).random()
    }.joinToString("")
    private fun randomBio(): String = List(80) {
        (('a'..'z')).random()
    }.joinToString("")

    fun prepareChatData(userId: String, messageId: String): Chat {
        val chat = Chat(
            id = ObjectId.get().toString(),
            name = randomName(),
            userIds = setOf(userId) as HashSet,
            messageIds = mutableListOf(messageId)
        )
        return chat
    }

    fun prepareUserData(): User{
        val user = User(
            id = ObjectId.get().toString(),
            name = randomName(),
            phoneNumber = "+" + randomPhone(),
            bio = randomBio(),
            chat = emptyList(),
            message = emptyList()
        )
        return user
    }
    private fun randomText(): String = List(80) {
        (('a'..'z')).random()
    }.joinToString("")

    fun prepareData(): Message {
        val message = Message(
            id = ObjectId.get().toString(),
            datetime = LocalDateTime.now().toString(),
            text = randomText(),
            messageChatId = ObjectId.get().toString(),
            messageUserId = ObjectId.get().toString()
        )
        return message
    }
    @Test
    fun `should create chat`(){
        val user = prepareUserData()
        val message = prepareData()
        val chat = prepareChatData(user.id!!, message.id!!)

        Mockito.`when`(chatRepository.save(chat)).thenReturn(Mono.just(chat))

        webClient.post().uri("http://localhost:8081/chat/chats-create")
            .header(HttpHeaders.ACCEPT, "application/json")
            .body(Mono.just(chat), Chat::class.java)
            .exchange()
            .expectStatus().isCreated
            .expectBody()

        Mockito.verify(chatRepository, Mockito.times(1)).save(chat)
    }

    @Test
    fun `should add user to chat`(){
        var user = prepareUserData()
        var message = prepareData()
        var chat = prepareChatData(user.id!!, message.id!!)
        Mockito.`when`(chatRepository.save(chat)).thenReturn(Mono.just(chat))
        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))


        webClient.put().uri("http://localhost:8081/chat/chats-add/{chatId}/{id}", chat.id, user.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .body(Mono.just(chat), Chat::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    @Test
    fun `should delete user from chat chat`(){
        val user = prepareUserData()
        val message = prepareData()
        val chat = prepareChatData(user.id!!, message.id!!)
        Mockito.`when`(chatRepository.save(chat)).thenReturn(Mono.just(chat))
        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))


        webClient.put().uri("http://localhost:8081/chat/chats-remove/{chatId}/{id}", chat.id, user.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .body(Mono.just(chat), Chat::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    @Test
    fun `should delete chat`(){
        val user = prepareUserData()
        val message = prepareData()
        val chat = prepareChatData(user.id!!, message.id!!)
        val empty: Mono<Void> = Mono.empty()

        Mockito.`when`(chatRepository.deleteById(chat.id!!)).thenReturn(empty)

        webClient.delete().uri("http://localhost:8081/chat/chats-delete/{id}", chat.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isOk

        Mockito.verify(chatRepository, Mockito.times(1)).deleteById(chat.id!!)
    }

    @Test
    fun `should get full chat by chat id`(){
        ReactorDebugAgent.init()
        var user = prepareUserData()
        var message = prepareData()
        var chat = prepareChatData(user.id!!, message.id!!)

        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
        Mockito.`when`(messageRepository.findById(message.id!!)).thenReturn(Mono.just(message))
        var fullchat = FullChat(chat, listOf(user), listOf(message))
        Mockito.`when`(fullChatRepository.save(any(FullChat::class.java))).thenReturn(fullchat.toMono())


        webClient.get().uri("http://localhost:8081/chat/get-chat-id/{id}", chat.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    @Test
    fun `should find all chats`(){
        val user = prepareUserData()
        val message = prepareData()
        val chat1 = prepareChatData(user.id!!, message.id!!)
        val chat2 = prepareChatData(user.id!!, message.id!!)
        val chat3 = prepareChatData(user.id!!, message.id!!)

        val list = listOf(chat1, chat2, chat3)
        val chatFlux = Flux.fromIterable(list)

        Mockito.`when`(chatRepository.findAll()).thenReturn(chatFlux)

        webClient.get().uri("http://localhost:8081/chat/chats-all")
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isFound
            .expectBodyList(Chat::class.java)

        Mockito.verify(chatRepository, Mockito.times(1)).findAll()
    }
}