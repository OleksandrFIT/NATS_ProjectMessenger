package com.example.reactiveproject.grpc


import com.example.reactiveproject.ReactorUserServiceGrpc
import com.example.reactiveproject.Services
import com.example.reactiveproject.model.User
import com.example.reactiveproject.repository.MessageRepository
import com.example.reactiveproject.repository.UserRepository
import com.example.reactiveproject.service.impl.UserServiceImpl
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import com.example.reactiveproject.helper.*
import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.Message
import com.google.protobuf.Empty
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Import(UserServiceImpl::class)
internal class GrpcUserControllerTest{

    @MockBean
    lateinit var userRepository: UserRepository

    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress("localhost", 6565)
        .usePlaintext()
        .build()

    private var serviceStub: ReactorUserServiceGrpc.ReactorUserServiceStub =
        ReactorUserServiceGrpc.newReactorStub(channel)


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
    fun prepareDataNoId(): User {
        val user = User(
            name = randomName(),
            phoneNumber = "+" + randomPhone(),
            bio = randomBio(),
            chat = emptyList(),
            message = emptyList()
        )
        return user
    }


    @Test
    fun `should return user by id`(){
        val user = prepareData()
        val grpcUser = userToGrpcUnMonoResponse(user)

        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))
        val request = Services.id.newBuilder().setId(user.id).build()
        val connect = serviceStub.findUserById(request)

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
        val connect = serviceStub.findUserByUserName(request)

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
        val connect = serviceStub.findUserByPhoneNumber(request)

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
            chat = listOf(Chat(
                name = "dfg",
                messageIds = mutableListOf("12","er"),
                userIds = mutableSetOf("123", "5")
            )),
            message = listOf(Message(
                text = "333",
                messageUserId = "1",
                messageChatId = "1"
            ))
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


         val connection = serviceStub.createUser(request)

        StepVerifier.create(connection)
            .expectNext(grpcUser)
            .verifyComplete()

    }

    @Test
    fun `should update user`(){
        val id = ObjectId.get().toString()
        var user = User(
            id = id,
            name = "Andrew",
            phoneNumber = "+3809888888888",
            bio = "dsdadfsdfsdf",
            chat = emptyList(),
            message = emptyList()
        )
        val userUp = User(
            id = id,
            name = "New name",
            phoneNumber = "+3809888888888",
            bio = "dsdadfsdfsdf",
            chat = emptyList(),
            message = emptyList()
        )
        Mockito.`when`(userRepository.save(ArgumentMatchers.any(User::class.java))).thenReturn(Mono.just(userUp))
        Mockito.`when`(userRepository.findById(id)).thenReturn(Mono.just(user))


        val request = Services.UserUpdateRequest.newBuilder().apply {
            userId = id
            setUser(Services.UserDescription.newBuilder().apply {
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
            }.build())
        }.build()

        val connection = serviceStub.updateUser(request)

        StepVerifier.create(connection)
            .expectNext(userToGrpcUnMonoResponse(userUp))
            .verifyComplete()

    }

    @Test
    fun `should delete user`(){
        val user = prepareData()
        val emptyReturn: Mono<Void> = Mono.empty<Void?>()
        val empty = Empty.newBuilder().build()

        Mockito.`when`(userRepository.deleteById(user.id!!)).thenReturn(emptyReturn)

        val request = Services.id.newBuilder().setId(user.id).build()
        val connect = serviceStub.deleteUser(request)

        val expected = Services.DeleteAnswer.newBuilder().apply { text = "User is deleted" }.build()

        StepVerifier.create(connect).expectNext(expected).verifyComplete()

    }
}