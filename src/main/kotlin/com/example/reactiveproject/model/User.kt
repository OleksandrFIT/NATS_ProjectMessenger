package com.example.reactiveproject.model


import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.Message
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.mapping.MongoId

@Document("users")
data class User(
    @MongoId(value = FieldType.OBJECT_ID)
    val id: String? = null,
    val name: String,
    val phoneNumber: String,
    val bio: String,

    val chat: List<Chat>,

    val message: List<Message>
)