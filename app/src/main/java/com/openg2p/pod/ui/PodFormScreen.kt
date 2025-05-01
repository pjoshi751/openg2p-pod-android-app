package com.openg2p.pod.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.openg2p.pod.R
import com.openg2p.pod.viewmodel.ImageData
import com.openg2p.pod.viewmodel.PodViewModel
import com.openg2p.pod.viewmodel.SubmissionState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PodFormScreen(
    viewModel: PodViewModel,
    context: Context = LocalContext.current,
    onNavigateToSettings: () -> Unit
) {

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val snackbarHostState = remember { SnackbarHostState() } // For showing messages

    // State from ViewModel
    val disbursementId by viewModel.disbursementId
    val agentId by viewModel.agentId
    val beneficiaryId by viewModel.beneficiaryId
    val latitude by viewModel.latitude
    val longitude by viewModel.longitude
    val geoJson by viewModel.geoJson
    val proofsJsonLd by viewModel.proofsJsonLd
    val images = viewModel.images // Note: Direct access to mutableStateList works
    val submissionState by viewModel.submissionState.collectAsState()
    val isLocationLoading by viewModel.isLocationLoading
    val canAddMoreImages by viewModel.canAddMoreImages // Get the state for enabling buttons

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // --- Permission Handling ---
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    // --- Camera Result Handling ---
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                viewModel.addImage(uri)
                tempImageUri = null // Reset temp URI
            }
        } else {
            // Handle camera cancellation or failure
            Log.d("PodFormScreen", "Camera cancelled or failed.")
            tempImageUri = null // Clean up temp URI if capture failed
        }
    }

    // --- File Picker Result Handling ---
     val pickImageLauncher = rememberLauncherForActivityResult(
          ActivityResultContracts.GetContent()
     ) { uri: Uri? ->
          uri?.let { viewModel.addImage(it) }
     }

    // Function to create URI for camera
    fun getTmpFileUri(context: Context): Uri {
         val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        // Use cache directory for temporary files
        val tmpFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", context.cacheDir).apply {
             createNewFile()
             deleteOnExit() // Ensure cleanup if app crashes
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", tmpFile)
    }

    // Side effect for handling permissions and location fetching
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
             Log.d("PodFormScreen", "All permissions granted.")
            if (latitude == null || longitude == null) {
                viewModel.getCurrentLocation(fusedLocationClient)
            }
        } else {
             Log.d("PodFormScreen", "Not all permissions granted. Requesting...")
             // You might want to trigger the request explicitly if not granted initially
             // permissionsState.launchMultiplePermissionRequest() // Can be called from a button click etc.
        }
    }

    // Effect to show Snackbar based on submission state
    LaunchedEffect(submissionState) {
        Log.d("PodFormScreen", "Submission state changed: $submissionState")
        when (val state = submissionState) {
            is SubmissionState.Success -> {
                Log.d("PodFormScreen", "Showing Success Snackbar: ${state.message}")
                snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = "SUCCESS",
                    duration = SnackbarDuration.Short // Auto-dismiss short
                )
                Log.d("PodFormScreen", "Snackbar shown, resetting state.")
                viewModel.resetSubmissionState() // Reset state after showing message
            }
            is SubmissionState.Error -> {
                Log.d("PodFormScreen", "Showing Error Snackbar: ${state.message}")
                snackbarHostState.showSnackbar(
                    message = "Error: ${state.message}",
                    actionLabel = "ERROR",
                    duration = SnackbarDuration.Long // Auto-dismiss long
                )
                Log.d("PodFormScreen", "Snackbar shown, resetting state.")
                viewModel.resetSubmissionState() // Reset state after showing message
            }
            else -> { Log.d("PodFormScreen", "State is Idle or Loading.") }
        }
    }

    val successColor = Color(0xFF4CAF50) // Define Green color

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proof of Delivery", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_openg2p_logo),
                        contentDescription = "OpenG2P Logo",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { // Customize Snackbar appearance
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                val isError = snackbarData.visuals.actionLabel == "ERROR"
                val containerColor = if (isError) {
                    MaterialTheme.colorScheme.errorContainer // Red for errors
                } else {
                    successColor // Green for success
                }
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = containerColor, // Apply custom color
                    // Optionally customize content color for better contrast
                    contentColor = if(isError) MaterialTheme.colorScheme.onErrorContainer else Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Make the form scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Request Permissions Button (shown if needed)
            if (!permissionsState.allPermissionsGranted) {
                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                    Text("Grant Permissions")
                }
                Text(
                    "Camera and Location permissions are required for this app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Required ID Fields
            OutlinedTextField(
                value = disbursementId,
                onValueChange = { viewModel.disbursementId.value = it },
                label = { Text("Disbursement ID*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = Color.Black
                )
            )
            OutlinedTextField(
                value = agentId,
                onValueChange = { viewModel.agentId.value = it },
                label = { Text("Agent ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = Color.Black
                )
            )
            OutlinedTextField(
                value = beneficiaryId,
                onValueChange = { viewModel.beneficiaryId.value = it },
                label = { Text("Beneficiary ID*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = Color.Black
                )
            )

            // Location Display and Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (latitude != null && longitude != null) "Lat: %.6f, Lon: %.6f".format(latitude, longitude) else "Location: Not available",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isLocationLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = {
                            if (permissionsState.allPermissionsGranted) {
                                viewModel.getCurrentLocation(fusedLocationClient)
                            } else {
                                // Show message or trigger permission request again
                                 permissionsState.launchMultiplePermissionRequest()
                            }
                        },
                        enabled = permissionsState.allPermissionsGranted // Enable only if permissions granted
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Refresh Location")
                    }
                }
            }

             // Image Capture Section
            Text("Photos (max 5)*", style = MaterialTheme.typography.titleMedium)
            ImagePreviewRow(
                 images = images,
                 onRemoveImage = { index -> viewModel.removeImage(index) },
                 onDescriptionChange = { index, description ->
                     viewModel.updateImageDescription(index, description)
                 }
            )
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.spacedBy(10.dp)
             ) {
                 Button(
                    onClick = {
                        if (permissionsState.permissions.first { it.permission == Manifest.permission.CAMERA }.status.isGranted) {
                             if (canAddMoreImages) { 
                                 tempImageUri = getTmpFileUri(context) // Get URI before launching
                                 tempImageUri?.let { takePictureLauncher.launch(it) }
                            } else {
                                // Show snackbar: max photos reached
                                 viewModel.viewModelScope.launch {
                                     snackbarHostState.showSnackbar("Maximum 5 photos allowed.")
                                 }
                            }
                        } else {
                             // Request camera permission specifically or show message
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    },
                     enabled = canAddMoreImages // Disable button if limit reached
                ) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Camera")
                }
                 Button(
                     onClick = {
                         if (canAddMoreImages) { 
                             pickImageLauncher.launch("image/*")
                         } else {
                             viewModel.viewModelScope.launch {
                                 snackbarHostState.showSnackbar("Maximum 5 photos allowed.")
                             }
                         }
                    },
                     enabled = canAddMoreImages // Disable button if limit reached
                 ) {
                     Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                     Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                     Text("Gallery")
                 }
             }


            // Optional Fields
            OutlinedTextField(
                value = geoJson,
                onValueChange = { viewModel.geoJson.value = it },
                label = { Text("GeoJSON (Optional)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), // Allow multi-line
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                maxLines = 5,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = Color.Black
                )
            )
            OutlinedTextField(
                value = proofsJsonLd,
                onValueChange = { viewModel.proofsJsonLd.value = it },
                label = { Text("Proofs JSON-LD (Optional)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), // Allow multi-line
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                maxLines = 5,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = Color.Black
                )
            )

             Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = { viewModel.submitProof(context) },
                enabled = submissionState != SubmissionState.Loading && permissionsState.allPermissionsGranted, // Disable during loading or if permissions missing
                 modifier = Modifier.fillMaxWidth()
            ) {
                if (submissionState == SubmissionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                     Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                }
                Text("Submit Proof")
            }
        }
    }
}


@Composable
fun ImagePreviewRow(
    images: List<ImageData>,
    onRemoveImage: (Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(images) { index, imageData -> 
            ImagePreviewItem(
                imageData = imageData, 
                index = index, 
                onRemoveClick = { onRemoveImage(index) },
                onDescriptionChange = { newDescription -> onDescriptionChange(index, newDescription) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewItem(
    imageData: ImageData,
    index: Int, 
    onRemoveClick: () -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    var description by remember { mutableStateOf(imageData.description) }

     Column(horizontalAlignment = Alignment.CenterHorizontally) {
         Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageData.uri),
                contentDescription = "Selected image ${index + 1}",
                modifier = Modifier
                    .fillMaxSize(), // Fill the box
                contentScale = ContentScale.Crop // Crop to fit
            )
             // Remove Button
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd) // Use align from BoxScope
                    .padding(4.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove image",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
         // Basic description field below image
         TextField(
             value = description,
            onValueChange = { newValue ->
                description = newValue // Update local state
                onDescriptionChange(newValue) // Notify parent/ViewModel
            },
             label = { Text("Description for Image") },
             modifier = Modifier.width(120.dp).padding(top = 4.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
            singleLine = true
         )
     }
}


// Helper function to check permissions
fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
