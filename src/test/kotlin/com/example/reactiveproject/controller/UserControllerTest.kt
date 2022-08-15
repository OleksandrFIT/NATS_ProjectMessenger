package com.example.reactiveproject.controller

import com.example.reactiveproject.model.User
import com.example.reactiveproject.repository.UserRepository
import com.example.reactiveproject.service.impl.UserServiceImpl
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [UserController::class])
@Import(UserServiceImpl::class)
internal class UserControllerTest{

    @MockBean
    private lateinit var userRepository: UserRepository

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


    fun prepareData(): User{
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


    @Test
    fun `should create user`(){
        val user = prepareData()

        Mockito.`when`(userRepository.save(user)).thenReturn(Mono.just(user))

        webClient.post().uri("http://localhost:8081/user/users-create")
            .header(HttpHeaders.ACCEPT, "application/json")
            .body(Mono.just(user), User::class.java)
            .exchange()
            .expectStatus().isCreated
            .expectBody()

        Mockito.verify(userRepository, Mockito.times(1)).save(user)

    }

    @Test
    fun `should update user`(){
        val user = prepareData()
        Mockito.`when`(userRepository.save(user)).thenReturn(Mono.just(user))
        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))

        val userUp = userRepository.findById(user.id!!)
            .map {
                User(
                    id = user.id,
                    name = user.name,
                    phoneNumber = user.phoneNumber,
                    bio = user.bio,
                    chat = user.chat,
                    message = user.message
                )
            }
            .flatMap(
                userRepository::save
            )
            .subscribe()

        webClient.put().uri("http://localhost:8081/user/users-update/{id}", user.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .body(Mono.just(user), User::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    @Test
    fun `should delete user`(){
        val user = prepareData()
        val empty: Mono<Void> = Mono.empty<Void?>()

        Mockito.`when`(userRepository.deleteById(user.id!!)).thenReturn(empty)

        webClient.delete().uri("http://localhost:8081/user/users-delete/{id}", user.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isOk

        Mockito.verify(userRepository, Mockito.times(1)).deleteById(user.id!!)
    }
    @Test
    fun `should find user by id`(){
        val user = prepareData()

        Mockito.`when`(userRepository.findById(user.id!!)).thenReturn(Mono.just(user))

        //userService.findByUserId(prepareData().id!!).subscribe()

        webClient.get().uri("http://localhost:8081/user/users-find/{id}", user.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(User::class.java)

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.id!!)
    }

    @Test
    fun `should find user by user name`(){
        val user = prepareData()

        Mockito.`when`(userRepository.findByName(user.name)).thenReturn(Mono.just(user))

        webClient.get().uri("http://localhost:8081/user/users/by-name/{userName}", user.name)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(User::class.java)

        Mockito.verify(userRepository, Mockito.times(1)).findByName(user.name)
    }

    @Test
    fun `should find user by phone number`(){
        val user = prepareData()

        Mockito.`when`(userRepository.findByPhoneNumber(user.phoneNumber)).thenReturn(Mono.just(user))

        webClient.get().uri("http://localhost:8081/user/users/by-phone/{phoneNumber}", user.phoneNumber)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(User::class.java)

        Mockito.verify(userRepository, Mockito.times(1)).findByPhoneNumber(user.phoneNumber)
    }






}