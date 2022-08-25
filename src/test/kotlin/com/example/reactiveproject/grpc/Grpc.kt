package com.example.reactiveproject.grpc

import com.example.reactiveproject.ReactorChatServiceGrpc
import com.example.reactiveproject.ReactorMessageServiceGrpc
import com.example.reactiveproject.ReactorUserServiceGrpc
import com.example.reactiveproject.Services
import com.example.reactiveproject.config.Configuration
import com.example.reactiveproject.helper.*
import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.FullChat
import com.example.reactiveproject.model.Message
import com.example.reactiveproject.model.User
import com.example.reactiveproject.redisService.MessageRedisService
import com.example.reactiveproject.repository.ChatRepository
import com.example.reactiveproject.repository.FullChatRepository
import com.example.reactiveproject.repository.MessageRepository
import com.example.reactiveproject.repository.UserRepository
import com.example.reactiveproject.service.impl.ChatServiceImpl
import com.example.reactiveproject.service.impl.MessageServiceImpl
import com.example.reactiveproject.service.impl.UserServiceImpl
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
//import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.given
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.time.LocalDateTime


@SpringBootTest
@ExtendWith(SpringExtension::class)
@Import(
    ChatServiceImpl::class,
    UserServiceImpl::class,
    MessageServiceImpl::class,
    Configuration::class
)
internal class Grpc {

//    @InjectMocks
//    lateinit var messageServiceImpl: MessageServiceImpl
    @MockBean
    lateinit var chatRepository: ChatRepository
    @MockBean
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var messageRepository: MessageRepository

    @MockBean
    private lateinit var fullChatRepository: FullChatRepository
    @MockBean
    private lateinit var messageRedisServiceImpl: MessageRedisService
    @MockBean
    private lateinit var reactiveRedisTemplateMessage: ReactiveRedisTemplate<String, Services.MessageResponse>


    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress("localhost", 6565)
        .usePlaintext()
        .build()

    private var serviceStubChat: ReactorChatServiceGrpc.ReactorChatServiceStub =
        ReactorChatServiceGrpc.newReactorStub(channel)

    private var serviceStubMessage: ReactorMessageServiceGrpc.ReactorMessageServiceStub =
        ReactorMessageServiceGrpc.newReactorStub(channel)

    private var serviceStubUser: ReactorUserServiceGrpc.ReactorUserServiceStub =
        ReactorUserServiceGrpc.newReactorStub(channel)

    //prepare data for USER
    fun randomName(): String = List(10) {
        (('a'..'z') + ('A'..'Z')).random()
    }.joinToString("")
    private fun randomPhone(): String = List(12) {
        (('0'..'9')).random()
    }.joinToString("")
    private fun randomBio(): String = List(80) {
        (('a'..'z')).random()
    }.joinToString("")


    fun prepareData(): User {
        return User(
            id = ObjectId.get().toString(),
            name = randomName(),
            phoneNumber = "+" + randomPhone(),
            bio = randomBio(),
            chat = emptyList(),
            message = emptyList()
        )
    }
    // prepare data for MESSAGE
    private fun randomText(): String = List(80) {
        (('a'..'z')).random()
    }.joinToString("")

    fun prepareDataMessage(): Message {
        return Message(
            id = ObjectId.get().toString(),
            datetime = LocalDateTime.now().toString(),
            text = randomText(),
            messageChatId = ObjectId.get().toString(),
            messageUserId = ObjectId.get().toString()
        )
    }

    //prepare data for Chat
    fun prepareChatData(userId: String, messageId: String): Chat {
        return Chat(
            id = ObjectId.get().toString(),
            name = randomName(),
            userIds = mutableSetOf(userId),
            messageIds = mutableListOf(messageId)
        )
    }

    //Tests for USER
    @Test
    fun `should return user by id`(){
        val user = prepareData()
        val grpcUser = userToGrpcUnMonoResponse(user)

        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
        val request = Services.id.newBuilder().setId(user.id).build()
        val connect = serviceStubUser.findUserById(request)

        StepVerifier.create(connect)
            .expectNext(grpcUser)
            .verifyComplete()
    }

    @Test
    fun `should return user by userName`(){
        val user = prepareData()
        val grpcUser = userToGrpcUnMonoResponse(user)

        Mockito.`when`(userRepository.findByName(user.name)).thenReturn(Mono.just(user))
        val request = Services.name.newBuilder().setName(user.name).build()
        val connect = serviceStubUser.findUserByUserName(request)

        StepVerifier.create(connect)
            .expectNext(grpcUser)
            .verifyComplete()
    }

    @Test
    fun `should find user by phone number`(){
        val user = prepareData()
        val grpcUser = userToGrpcUnMonoResponse(user)

        Mockito.`when`(userRepository.findByPhoneNumber(user.phoneNumber)).thenReturn(Mono.just(user))
        val request = Services.phoneNumber.newBuilder().setPhoneNumber(user.phoneNumber).build()
        val connect = serviceStubUser.findUserByPhoneNumber(request)

        StepVerifier.create(connect)
            .expectNext(grpcUser)
            .verifyComplete()
    }

    @Test
    fun `should create user`(){
        val userid = ObjectId.get().toString()
        val user = User(
            id = userid,
            name = "Andrew",
            phoneNumber = "+3809888888888",
            bio = "dsdadfsdfsdf",
            chat = listOf(
                Chat(
                name = "dfg",
                messageIds = mutableListOf("12","er"),
                userIds = mutableSetOf("123", "5")
            )
            ),
            message = listOf(
                Message(
                text = "333",
                messageUserId = "1",
                messageChatId = "1"
            )
            )
        )
        val grpcUser = userToGrpcUnMonoResponse(user)

        Mockito.`when`(userRepository.save(ArgumentMatchers.any(User::class.java))).thenReturn(Mono.just(user))
        val request = Services.UserDescription.newBuilder().apply {

            name = "Andrew"
            phoneNumber = "+3809888888888"
            bio = "dsdadfsdfsdf"
            addAllChat(listOf(Services.ChatDescription.newBuilder().apply {
                name = "dfg"
                addAllMessageIds(listOf("12", "er"))
                addAllUserIds(listOf("123", "5"))
            }.build()))
            addAllMessage(listOf(Services.MessageDescription.newBuilder().apply {
                text = "333"
                messageUserId = "1"
                messageChatId = "1"
            }.build()))
        }.build()


        val connection = serviceStubUser.createUser(request)

        StepVerifier.create(connection)
            .expectNext(grpcUser)
            .verifyComplete()

    }

//    @Test
//    fun `should update user`(){
//        val id = ObjectId.get().toString()
//        val user = User(
//            id = id,
//            name = "Andrew",
//            phoneNumber = "+3809888888888",
//            bio = "dsdadfsdfsdf",
//            chat = emptyList(),
//            message = emptyList()
//        )
//        val userUp = User(
//            id = id,
//            name = "New name",
//            phoneNumber = "+3809888888888",
//            bio = "dsdadfsdfsdf",
//            chat = emptyList(),
//            message = emptyList()
//        )
//        Mockito.`when`(userRepository.save(ArgumentMatchers.any(User::class.java))).thenReturn(Mono.just(userUp))
//        Mockito.`when`(userRepository.findById(id)).thenReturn(Mono.just(user))
//
//
//        val request = Services.UserUpdateRequest.newBuilder().apply {
//            userId = id
//            setUser(Services.UserDescription.newBuilder().apply {
//                name = "Andrew"
//                phoneNumber = "+3809888888888"
//                bio = "dsdadfsdfsdf"
//                addAllChat(listOf(Services.ChatDescription.newBuilder().apply {
//                    name = "dfg"
//                    addAllMessageIds(listOf("12", "er"))
//                    addAllUserIds(listOf("123", "5"))
//                }.build()))
//                addAllMessage(listOf(Services.MessageDescription.newBuilder().apply {
//                    text = "333"
//                    messageUserId = "1"
//                    messageChatId = "1"
//                }.build()))
//            }.build())
//        }.build()
//
//        val connection = serviceStubUser.updateUser(request)
//
//        StepVerifier.create(connection)
//            .expectNext(userToGrpcUnMonoResponse(userUp))
//            .verifyComplete()
//
//    }

    @Test
    fun `should delete user`(){
        val user = prepareData()
        val emptyReturn: Mono<Void> = Mono.empty()

        Mockito.`when`(userRepository.deleteById(user.id!!)).thenReturn(emptyReturn)

        val request = Services.id.newBuilder().setId(user.id).build()
        val connect = serviceStubUser.deleteUser(request)

        val expected = Services.DeleteAnswer.newBuilder().apply { text = "User is deleted" }.build()

        StepVerifier.create(connect).expectNext(expected).verifyComplete()

    }

    //Tests for MESSAGE


//
//    @Test
//    fun `should update message`(){
//        val mesId = ObjectId.get().toString()
//        val date = LocalDateTime.now().toString()
//        val message = Message(
//            id = mesId,
//            datetime = date,
//            text = "ddddddddddd",
//            messageChatId = "1",
//            messageUserId = "1"
//        )
//
//        val messageUp = Message(
//            id = mesId,
//            datetime = date,
//            text = "newwwwwwww",
//            messageChatId = "1",
//            messageUserId = "1"
//        )
//
//        Mockito.`when`(messageRepository.save(any(Message::class.java))).thenReturn(Mono.just(messageUp))
//        Mockito.`when`(messageRepository.findMessageById(message.id!!)).thenReturn(Mono.just(message))
//
//        val grpcUserRequest = messageToGrpc(message)
//        val request = Services.MessageUpdateRequest
//            .newBuilder()
//            .apply {
//                messageId = mesId
//                messageBuilder.apply {
//                    text = grpcUserRequest.text
//                }
//            }.build()
//
//
//        val connection = serviceStubMessage.editMessage(request)
//        StepVerifier.create(connection)
//            .expectNext(messageToGrpcUnMono(messageUp))
//            .verifyComplete()
//
//    }


//    @Test
//    fun `should delete message`() {
//        val message = prepareDataMessage()
//        val emptyReturn: Mono<Void> = Mono.empty()
//
//        Mockito.`when`(messageRepository.deleteById(message.id!!)).thenReturn(emptyReturn)
//
//        val request = Services.id.newBuilder().setId(message.id).build()
//        val connect = serviceStubMessage.deleteMessage(request)
//
//        val expected = Services.DeleteAnswer.newBuilder().apply { text = "Message is deleted" }.build()
//
//        StepVerifier.create(connect).expectNext(expected).verifyComplete()
//
//    }

    //Tests for CHAT
//    @Test


//    @Test
//    fun `should add user to chat`(){
//        val user = prepareData()
//        val message = prepareDataMessage()
//        val chat = prepareChatData(user.id!!, message.id!!)
//        Mockito.`when`(chatRepository.save(ArgumentMatchers.any(Chat::class.java))).thenReturn(Mono.just(chat))
//        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
//        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
//
//        val request = Services.ChatUpdateRequest.newBuilder().apply {
//            chatId = chat.id
//            userId = user.id
//        }.build()
//        val connect = serviceStubChat.addUserToTheChat(request)
//
//        StepVerifier.create(connect)
//            .expectNext(chatToGrpc(chat))
//            .verifyComplete()
//    }

    @Test
    fun `should get full chat by chat id`(){
        val user = prepareData()
        val message = prepareDataMessage()
        val chat = prepareChatData(user.id!!, message.id!!)
        val fullchat = FullChat(chat, listOf(user), listOf(message))

        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
        Mockito.`when`(messageRepository.findById(message.id!!)).thenReturn(Mono.just(message))
        Mockito.`when`(fullChatRepository.save(ArgumentMatchers.any(FullChat::class.java))).thenReturn(fullchat.toMono())

        val request = Services.id.newBuilder().setId(chat.id).build()
        val connect = serviceStubChat.getChatById(request)

        StepVerifier.create(connect)
            .expectNext(
                fullChatToGrpcUnMono(fullchat)
            )
            .verifyComplete()
    }

    @Test
    fun `should create chat`(){
        val user = prepareData()
        val message = prepareDataMessage()
        val chat = prepareChatData(user.id!!, message.id!!)
        val grpcChat = chatToGrpc(chat)

        Mockito.`when`(chatRepository.save(any(Chat::class.java))).thenReturn(Mono.just(chat))

        val request =
            Services.ChatDescription.newBuilder().apply {
                name = chat.name
                addAllMessageIds(chat.messageIds)
                addAllUserIds(chat.userIds)
            }.build()

        val connect = serviceStubChat.createChat(request)

        StepVerifier.create(connect)
            .expectNext(grpcChat)
            .verifyComplete()

    }
//    @Test
//    fun `should create message`() {
//        val message = prepareDataMessage()
//        val grpcMessage = messageToGrpcUnMono(message)
//
//        //Mockito.`when`(messageRepository.save(any(Message::class.java))).thenReturn(Mono.just(message))
//        given(messageRepository.save(any())).willReturn(Mono.just(message))
//        val request =
//            Services.MessageDescription
//                .newBuilder()
//                .apply {
//                text = message.text
//                messageChatId = message.messageChatId
//                messageUserId = message.messageUserId
//            }.build()
//
//        val connect = serviceStubMessage.sendMessage(request)
//
//        StepVerifier.create(connect)
//            .expectNext(grpcMessage)
//            .verifyComplete()
//
//    }
//
//    @Test
//    fun `should delete user from chat`(){
//        val user = prepareData()
//        val message = prepareDataMessage()
//        val chat = prepareChatData(user.id!!, message.id!!)
//        Mockito.`when`(chatRepository.save(ArgumentMatchers.any(Chat::class.java))).thenReturn(Mono.just(chat))
//        Mockito.`when`(chatRepository.findChatById(chat.id!!)).thenReturn(Mono.just(chat))
//        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
//
//        val request = Services.ChatUpdateRequest.newBuilder().apply {
//            chatId = chat.id
//            userId = user.id
//        }.build()
//        val connect = serviceStubChat.deleteUserFromChat(request)
//
//        StepVerifier.create(connect)
//            .expectNext(chatToGrpc(chat))
//            .verifyComplete()
//    }

    @Test
    fun `should delete chat`(){
        val user = prepareData()
        val message = prepareDataMessage()
        val chat = prepareChatData(user.id!!, message.id!!)
        val emptyReturn: Mono<Void> = Mono.empty()

        Mockito.`when`(chatRepository.deleteById(chat.id!!)).thenReturn(emptyReturn)

        val request = Services.id.newBuilder().setId(chat.id).build()
        val connect = serviceStubChat.deleteChat(request)

        val expected = Services.DeleteAnswer.newBuilder().apply { text = "Chat is deleted" }.build()

        StepVerifier.create(connect).expectNext(expected).verifyComplete()

    }

}