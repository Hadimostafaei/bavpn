package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "device_id") val deviceId: String = "android"
)

@JsonClass(generateAdapter = true)
data class UserDetails(
    @Json(name = "id") val id: Int,
    @Json(name = "username") val username: String,
    @Json(name = "is_admin") val isAdmin: Boolean
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "user") val user: UserDetails?
)

@JsonClass(generateAdapter = true)
data class ConfigModel(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "flag") val flag: String?,
    @Json(name = "flag_emoji") val flagEmoji: String?,
    @Json(name = "type") val type: String,
    @Json(name = "raw") val raw: String,
    @Json(name = "priority") val priority: Int
)

@JsonClass(generateAdapter = true)
data class ConfigListResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "configs") val configs: List<ConfigModel>
)
