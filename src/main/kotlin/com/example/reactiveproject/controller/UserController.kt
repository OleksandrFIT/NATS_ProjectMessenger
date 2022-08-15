package com.example.reactiveproject.controller

import com.example.reactiveproject.model.User
import com.example.reactiveproject.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/user")
open class UserController(
    val userService: UserService
) {

    @GetMapping("/users-find/{id}")
    fun findByUserId(@PathVariable("id") id: String): Mono<User?> {
        return userService.findByUserId(id)
    }

    @GetMapping("/users/by-phone/{phoneNumber}")
    fun findByPhoneNumber(@PathVariable("phoneNumber") phoneNumber: String): Mono<User?> {
        return userService.findUserByPhoneNumber(phoneNumber)
    }

    @GetMapping("/users/by-name/{userName}")
    fun findByUserName(@PathVariable("userName") userName: String): Mono<User?> {
        return userService.findUserByUserName(userName)
    }

    @PostMapping("/users-create")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody user: User) {
        userService.createUser(user)
    }

    @PutMapping("/users-update/{id}")
    fun updateUser(@PathVariable("id") id: String, @RequestBody user: User): Mono<User> {
        return userService.updateUser(id, user)
    }

    @DeleteMapping("/users-delete/{id}")
    fun delete(@PathVariable("id") id: String) {
        userService.deleteUser(id)
    }

}