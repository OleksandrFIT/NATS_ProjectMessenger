//package com.example.reactiveproject.grpc
//
//import com.example.reactiveproject.ReactorChatServiceGrpc
//import com.example.reactiveproject.Services
//import com.example.reactiveproject.model.Chat
//import com.example.reactiveproject.model.Message
//import com.example.reactiveproject.model.User
//import com.example.reactiveproject.repository.ChatRepository
//import com.example.reactiveproject.repository.FullChatRepository
//import com.example.reactiveproject.repository.MessageRepository
//import com.example.reactiveproject.repository.UserRepository
//import com.example.reactiveproject.service.impl.ChatServiceImpl
//import com.example.reactiveproject.service.impl.UserServiceImpl
//import com.google.protobuf.Empty
//import io.grpc.ManagedChannel
//import io.grpc.ManagedChannelBuilder
//import org.bson.types.ObjectId
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.Mockito
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.context.annotation.Import
//import org.springframework.http.HttpHeaders
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import reactor.core.publisher.Flux
//import reactor.kotlin.core.publisher.toFlux
//import reactor.test.StepVerifier
//import java.time.LocalDateTime
//import com.example.reactiveproject.helper.*
//import com.example.reactiveproject.model.FullChat
//import org.mockito.ArgumentMatchers
//import reactor.core.publisher.Mono
//import reactor.kotlin.core.publisher.toMono
//import reactor.tools.agent.ReactorDebugAgent
//
//@SpringBootTest
//@ExtendWith(SpringExtension::class)
//@Import(ChatServiceImpl::class)
//internal class GrpcChatServiceTest{
//
//    @MockBean
//    lateinit var chatRepository: ChatRepository
//    @MockBean
//    private lateinit var userRepository: UserRepository
//    @MockBean
//    private lateinit var messageRepository: MessageRepository
//    @MockBean
//    private lateinit var fullChatRepository: FullChatRepository
//
//    private val channel: ManagedChannel = ManagedChannelBuilder
//        .forAddress("localhost", 6565)
//        .usePlaintext()
//        .build()
//
//    private var serviceStub: ReactorChatServiceGrpc.ReactorChatServiceStub =
//        ReactorChatServiceGrpc.newReactorStub(channel)
//
//    fun randomName(): String = List(10) {
//        (('a'..'z') + ('A'..'Z')).random()
//    }.joinToString("")
//    private fun randomPhone(): String = List(12) {
//        (('0'..'9')).random()
//    }.joinToString("")
//    private fun randomBio(): String = List(80) {
//        (('a'..'z')).random()
//    }.joinToString("")
//
//    fun prepareChatData(userId: String, messageId: String): Chat {
//        val chat = Chat(
//            id = ObjectId.get().toString(),
//            name = randomName(),
//            userIds = mutableSetOf(userId),
//            messageIds = mutableListOf(messageId)
//        )
//        return chat
//    }
//
//    fun prepareUserData(): User {
//        val user = User(
//            id = ObjectId.get().toString(),
//            name = randomName(),
//            phoneNumber = "+" + randomPhone(),
//            bio = randomBio(),
//            chat = emptyList(),
//            message = emptyList()
//        )
//        return user
//    }
//    private fun randomText(): String = List(80) {
//        (('a'..'z')).random()
//    }.joinToString("")
//
//    fun prepareData(): Message {
//        val message = Message(
//            id = ObjectId.get().toString(),
//            datetime = LocalDateTime.now().toString(),
//            text = randomText(),
//            messageChatId = ObjectId.get().toString(),
//            messageUserId = ObjectId.get().toString()
//        )
//        return message
//    }
//
//
//    @Test
//    fun `should find all chats`() {
//        val user = prepareUserData()
//        val message = prepareData()
//        val chat1 = prepareChatData(user.id!!, message.id!!)
//        val chat2 = prepareChatData(user.id!!, message.id!!)
//        val chat3 = prepareChatData(user.id!!, message.id!!)
//
//        val grpcChat1 = chatToGrpc(chat1)
//        val grpcChat2 = chatToGrpc(chat2)
//        val grpcChat3 = chatToGrpc(chat3)
//
//        val list = listOf(chat1, chat2, chat3).toFlux()
//
//        Mockito.`when`(chatRepository.findAll()).thenReturn(list)
//        val connect = serviceStub.findAllChats(Empty.getDefaultInstance())
//
//        StepVerifier.create(connect).expectNext(
//            grpcChat1,
//            grpcChat2,
//            grpcChat3
//        ).verifyComplete()
//
//    }
//
//    @Test
//    fun `should add user to chat`(){
//        var user = prepareUserData()
//        var message = prepareData()
//        var chat = prepareChatData(user.id!!, message.id!!)
//        Mockito.`when`(chatRepository.save(ArgumentMatchers.any(Chat::class.java))).thenReturn(Mono.just(chat))
//        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
//        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
//
//        val request = Services.ChatUpdateRequest.newBuilder().apply {
//            chatId = chat.id
//            userId = user.id
//        }.build()
//        val connect = serviceStub.addUserToTheChat(request)
//
//        StepVerifier.create(connect)
//            .expectNext(chatToGrpc(chat))
//            .verifyComplete()
//    }
//
//
//    @Test
//    fun `should get full chat by chat id`(){
//        val user = prepareUserData()
//        val message = prepareData()
//        val chat = prepareChatData(user.id!!, message.id!!)
//        val fullchat = FullChat(chat, listOf(user), listOf(message))
//
//        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
//        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
//        Mockito.`when`(messageRepository.findById(message.id!!)).thenReturn(Mono.just(message))
//        Mockito.`when`(fullChatRepository.save(ArgumentMatchers.any(FullChat::class.java))).thenReturn(fullchat.toMono())
//
//        val request = Services.id.newBuilder().setId(chat.id).build()
//        val connect = serviceStub.getChatById(request)
//
//        StepVerifier.create(connect)
//            .expectNext(
//                fullChatToGrpcUnMono(fullchat)
//            )
//            .verifyComplete()
//    }
//
//    @Test
//    fun `should create chat`(){
//        val user = prepareUserData()
//        val message = prepareData()
//        val chat = prepareChatData(user.id!!, message.id!!)
//        val grpcChat = chatToGrpc(chat)
//
//        Mockito.`when`(chatRepository.save(ArgumentMatchers.any(Chat::class.java))).thenReturn(Mono.just(chat))
//
//        val request =
//            Services.ChatDescription.newBuilder().apply {
//                name = chat.name
//                addAllMessageIds(chat.messageIds)
//                addAllUserIds(chat.userIds)
//            }.build()
//
//        val connect = serviceStub.createChat(request)
//
//        StepVerifier.create(connect)
//            .expectNext(grpcChat)
//            .verifyComplete()
//
//    }
//
//    @Test
//    fun `should delete user from chat`(){
//        var user = prepareUserData()
//        var message = prepareData()
//        var chat = prepareChatData(user.id!!, message.id!!)
//        Mockito.`when`(chatRepository.save(ArgumentMatchers.any(Chat::class.java))).thenReturn(Mono.just(chat))
//        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
//        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
//
//        val request = Services.ChatUpdateRequest.newBuilder().apply {
//            chatId = chat.id
//            userId = user.id
//        }.build()
//        val connect = serviceStub.deleteUserFromChat(request)
//
//        StepVerifier.create(connect)
//            .expectNext(chatToGrpc(chat))
//            .verifyComplete()
//    }
//
//    @Test
//    fun `should delete chat`(){
//        val user = prepareUserData()
//        val message = prepareData()
//        val chat = prepareChatData(user.id!!, message.id!!)
//        val emptyReturn: Mono<Void> = Mono.empty<Void?>()
//        val empty = Empty.newBuilder().build()
//
//        Mockito.`when`(chatRepository.deleteById(chat.id!!)).thenReturn(emptyReturn)
//
//        val request = Services.id.newBuilder().setId(chat.id).build()
//        val connect = serviceStub.deleteChat(request)
//
//        val expected = Services.DeleteAnswer.newBuilder().apply { text = "Chat is deleted" }.build()
//
//        StepVerifier.create(connect).expectNext(expected).verifyComplete()
//
//    }
//
//}