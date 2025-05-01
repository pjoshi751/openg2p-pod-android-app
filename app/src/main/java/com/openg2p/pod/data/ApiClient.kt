package com.openg2p.pod.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    fun getClient(baseUrl: String): ApiService {
        // Basic validation for base URL
        if (baseUrl.isBlank() || !baseUrl.startsWith("http")) {
            throw IllegalArgumentException("Invalid Base URL provided: $baseUrl")
        }
        
        // Ensure base URL ends with a slash
        val validBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request/response bodies (useful for debugging)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(validBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Using Gson for potential response bodies if needed later
            .build()
            .create(ApiService::class.java)
    }
}
