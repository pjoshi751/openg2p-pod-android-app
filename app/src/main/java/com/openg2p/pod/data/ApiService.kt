package com.openg2p.pod.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface ApiService {

    @Multipart
    @POST("/api/v1/submit_proof") // Updated path with prefix
    suspend fun submitProof(
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photos: List<MultipartBody.Part>
    ): Response<Void> // Assuming the API returns 2xx on success with no body

}
