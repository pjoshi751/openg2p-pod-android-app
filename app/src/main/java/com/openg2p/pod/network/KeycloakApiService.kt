package com.openg2p.pod.network

import com.openg2p.pod.network.data.TokenResponse
import retrofit2.Response // Import Retrofit Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface KeycloakApiService {

    @FormUrlEncoded
    @POST
    suspend fun getToken(
        @Url url: String, // Allow dynamic URL from settings
        @Field("client_id") clientId: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): Response<TokenResponse> // Use Response<T> to handle success/error cases
}
