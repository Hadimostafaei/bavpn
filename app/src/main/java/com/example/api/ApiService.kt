package com.example.api

import com.example.models.ConfigListResponse
import com.example.models.LoginRequest
import com.example.models.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("configs")
    suspend fun getConfigs(
        @Header("Authorization") token: String
    ): ConfigListResponse
}
