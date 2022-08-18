package com.example.reactiveproject.grpc
import com.example.reactiveproject.ReactorUserServiceGrpc
import com.example.reactiveproject.Services
import com.example.reactiveproject.UserServiceGrpcKt
import com.example.reactiveproject.helper.*
import com.example.reactiveproject.service.UserService
import com.google.protobuf.Empty
import com.google.protobuf.empty
import org.lognet.springboot.grpc.GRpcService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono


@GRpcService
class GrpcUserService: ReactorUserServiceGrpc.UserServiceImplBase(){
    @Autowired
//    @Qualifier("userRedisServiceImpl")
    lateinit var userService: UserService

    override fun findUserById(request: Mono<Services.id>?): Mono<Services.UserResponse> {
        return idToGrpc(request!!)
            .flatMap { userService.findByUserId(it) }
            .map { userToGrpcUnMonoResponse(it) }
    }

    override fun findUserByUserName(request: Mono<Services.name>?): Mono<Services.UserResponse> {
        return userNameToGrpc(request!!)
            .flatMap {userService.findUserByUserName(it)}
            .map { userToGrpcUnMonoResponse(it) }

    }

    override fun findUserByPhoneNumber(request: Mono<Services.phoneNumber>?): Mono<Services.UserResponse> {
        return phoneNumberToGrpc(request!!)
            .flatMap {userService.findUserByPhoneNumber(it)}
            .map { userToGrpcUnMonoResponse(it) }
    }

    override fun createUser(request: Mono<Services.UserDescription>?): Mono<Services.UserResponse> {
        return grpcToUser(request!!).log("1")
            .flatMap { userService.createUser(it) }.log("2")
            .map { userToGrpcUnMonoResponse(it) }.log("3")
    }


    override fun deleteUser(request: Mono<Services.id>?): Mono<Services.DeleteAnswer> {
        return idToGrpc(request!!)
            .flatMap { userService.deleteUser(it) }
            .then(Services.DeleteAnswer.newBuilder().apply { text = "User is deleted" }.build().toMono())
    }

    override fun updateUser(request: Mono<Services.UserUpdateRequest>?): Mono<Services.UserResponse> {
        return updateGrpcToUser(request)
            .flatMap { userService.updateUser(it.first, it.second)}
            .map { updateUserToGrpcUnMono(it) }

    }
}