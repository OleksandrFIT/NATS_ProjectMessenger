package com.example.reactiveproject.repository

import com.example.reactiveproject.model.FullChat
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface FullChatRepository: ReactiveMongoRepository<FullChat, String>{

}