package com.openg2p.pod.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.openg2p.pod.data.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ImageData(val uri: Uri, val description: String = "")

sealed class SubmissionState {
    object Idle : SubmissionState()
    object Loading : SubmissionState()
    data class Success(val message: String) : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

class PodViewModel : ViewModel() {

    private val _tag = "PodViewModel"

    // --- Form State ---
    val serverUrl = mutableStateOf("http://10.0.2.2:8000") // Default for emulator
    val disbursementId = mutableStateOf("")
    val agentId = mutableStateOf("")
    val beneficiaryId = mutableStateOf("")
    val geoJson = mutableStateOf("") // Optional
    val proofsJsonLd = mutableStateOf("") // Optional
    val images = mutableStateListOf<ImageData>()

    // --- Location State ---
    val latitude = mutableStateOf<Double?>(null)
    val longitude = mutableStateOf<Double?>(null)
    val isLocationLoading = mutableStateOf(false)

    // --- Submission State ---
    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    fun addImage(uri: Uri) {
        if (images.size < 5) {
            images.add(ImageData(uri = uri))
        }
    }

    fun removeImage(index: Int) {
        if (index >= 0 && index < images.size) {
            images.removeAt(index)
        }
    }

    fun updateImageDescription(index: Int, description: String) {
        if (index >= 0 && index < images.size) {
            images[index] = images[index].copy(description = description)
        }
    }

    @SuppressLint("MissingPermission") // Permissions checked before calling
    fun getCurrentLocation(fusedLocationClient: FusedLocationProviderClient) {
        isLocationLoading.value = true
        Log.d(_tag, "Requesting current location...")
        // Use high accuracy and a cancellation token for a single update
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitude.value = location.latitude
                longitude.value = location.longitude
                Log.d(_tag, "Location Success: Lat=${location.latitude}, Lon=${location.longitude}")
            } else {
                Log.w(_tag, "Failed to get location: Location object was null")
                // Handle null location, maybe show error or retry?
            }
            isLocationLoading.value = false
        }.addOnFailureListener { e ->
            Log.e(_tag, "Failed to get location", e)
            isLocationLoading.value = false
            // Handle failure, show error message
        }.addOnCompleteListener { 
            // Ensure loading state is reset even if listener not called (e.g., permissions denied mid-request)
             isLocationLoading.value = false
        }
    }

    fun submitProof(context: Context) {
        if (!validateForm()) return

        _submissionState.value = SubmissionState.Loading
        viewModelScope.launch {
            try {
                val apiUrl = serverUrl.value
                if (apiUrl.isBlank()) {
                    _submissionState.value = SubmissionState.Error("Server URL cannot be empty")
                    return@launch
                }
                
                val apiService = ApiClient.getClient(apiUrl)

                // Prepare parts map
                val parts = mutableMapOf<String, RequestBody>()
                parts["disbursement_id"] = disbursementId.value.toRequestBody("text/plain".toMediaTypeOrNull())
                parts["agent_id"] = agentId.value.toRequestBody("text/plain".toMediaTypeOrNull())
                parts["beneficiary_id"] = beneficiaryId.value.toRequestBody("text/plain".toMediaTypeOrNull())
                parts["latitude"] = latitude.value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                parts["longitude"] = longitude.value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                
                // Add optional fields if not blank
                if (geoJson.value.isNotBlank()) {
                    parts["geojson"] = geoJson.value.toRequestBody("application/json".toMediaTypeOrNull())
                }
                if (proofsJsonLd.value.isNotBlank()) {
                    // Assuming proofs should be sent as plain text containing JSON-LD string
                    parts["proofs"] = proofsJsonLd.value.toRequestBody("application/ld+json".toMediaTypeOrNull()) 
                }

                // Prepare image parts and their descriptions
                val imageParts = mutableListOf<MultipartBody.Part>()
                val descriptionRequestBodies = mutableListOf<RequestBody>() // Prepare list for ApiService

                images.forEachIndexed { index, imageData ->
                    val compressedFile = compressImage(context, imageData.uri, index)
                    if (compressedFile != null) {
                        val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("photos", compressedFile.name, requestFile)
                        imageParts.add(body)
                        // Convert description to RequestBody and add to the list
                        descriptionRequestBodies.add(imageData.description.toRequestBody("text/plain".toMediaTypeOrNull()))
                    } else {
                        _submissionState.value = SubmissionState.Error("Failed to process image ${index + 1}")
                        return@launch // Stop submission if image processing fails
                    }
                }

                // We now pass descriptions as a separate parameter to the API service.
                val textParts = parts // The 'parts' map now only contains non-description, non-file fields.

                Log.d(_tag, "Submitting proof with parts: ${textParts.keys}, images: ${imageParts.size}, descriptions: ${descriptionRequestBodies.size}")

                // Make the API call using the updated ApiService signature
                val response = apiService.submitProof(textParts, imageParts, descriptionRequestBodies)

                if (response.isSuccessful) {
                    _submissionState.value = SubmissionState.Success("Proof submitted successfully!")
                    clearForm()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(_tag, "API Error: ${response.code()} - $errorBody")
                    _submissionState.value = SubmissionState.Error("Submission failed: ${response.code()} - $errorBody")
                }

            } catch (e: Exception) {
                Log.e(_tag, "Submission Exception", e)
                _submissionState.value = SubmissionState.Error("Submission failed: ${e.message}")
            }
        }
    }

    private fun validateForm(): Boolean {
        if (serverUrl.value.isBlank()) return false
        if (disbursementId.value.isBlank()) return false
        if (agentId.value.isBlank()) return false
        if (beneficiaryId.value.isBlank()) return false
        if (latitude.value == null || longitude.value == null) return false
        if (images.isEmpty()) return false
        // Basic JSON validation (optional but recommended)
        if (geoJson.value.isNotBlank() && !isValidJson(geoJson.value)) return false
        if (proofsJsonLd.value.isNotBlank() && !isValidJson(proofsJsonLd.value)) return false
        return true
    }
    
    // Very basic JSON check
    private fun isValidJson(jsonString: String): Boolean {
        return try {
            // A more robust check would involve a JSON library, but this catches basic syntax issues
            jsonString.trim().let { it.startsWith("{") && it.endsWith("}") || it.startsWith("[") && it.endsWith("]") }
        } catch (e: Exception) {
            false
        }
    }

    fun clearForm() {
        // Keep serverUrl
        disbursementId.value = ""
        agentId.value = ""
        beneficiaryId.value = ""
        latitude.value = null
        longitude.value = null
        images.clear()
        geoJson.value = ""
        proofsJsonLd.value = ""
        _submissionState.value = SubmissionState.Idle
    }

    fun resetSubmissionState() {
         _submissionState.value = SubmissionState.Idle
    }

    // --- Image Compression ---
    private suspend fun compressImage(context: Context, uri: Uri, index: Int): File? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) {
                     Log.e(_tag, "Could not decode bitmap from URI: $uri")
                     return@withContext null
                }

                val outputStream = ByteArrayOutputStream()
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Compress to JPEG, 80% quality
                val compressedBitmapData = outputStream.toByteArray()

                // Create a temporary file in cache dir
                 val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val tempFile = File.createTempFile("JPEG_${timeStamp}_${index}_", ".jpg", context.cacheDir)
                val fileOutputStream = FileOutputStream(tempFile)
                fileOutputStream.write(compressedBitmapData)
                fileOutputStream.close()

                Log.d(_tag, "Compressed image saved to: ${tempFile.absolutePath}, Size: ${tempFile.length() / 1024} KB")
                tempFile // Return the compressed file
            } catch (e: Exception) {
                Log.e(_tag, "Error compressing image $uri", e)
                null
            }
        }
    }
}
