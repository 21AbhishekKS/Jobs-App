import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: Int,
    navController: NavController
) {
    var job by remember { mutableStateOf<JobItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch job details when screen loads or jobId changes
    LaunchedEffect(jobId) {
        isLoading = true
        error = null

        try {
            val fetchedJob = withContext(Dispatchers.IO) {
                fetchJobDetails(jobId)
            }
            job = fetchedJob
        } catch (e: Exception) {
            error = e.message ?: "Failed to load job details"
        } finally {
            isLoading = false
        }
    }

    // Scaffold with back button
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Job Details") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                }
                job != null -> {
                    JobDetailContent(job = job!!)
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No job details found")
                    }
                }
            }
        }
    }
//}

@Composable
fun JobDetailContent(job: JobItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = job.title ?: "No Title", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Location: ${job.primary_details?.Place ?: "Not specified"}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Salary: ${job.primary_details?.Salary ?: "Not specified"}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Description:", style = MaterialTheme.typography.titleMedium)
        Text(text = job.description ?: "No description available")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Requirements:", style = MaterialTheme.typography.titleMedium)
        job.requirements?.forEach { requirement ->
            Text(text = "• $requirement")
        }
    }
}

    @kotlinx.serialization.Serializable
    data class JobsResponse(
        val results: List<JobItem>
    )

    private suspend fun fetchJobDetails(jobId: Int): JobItem {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
        }

        val response: JobsResponse = client.get("https://testapi.getlokalapp.com/common/jobs?page=1").body()

        return response.results.firstOrNull { it.id == jobId }
            ?: throw Exception("Job with ID $jobId not found")
    }



@kotlinx.serialization.Serializable
data class JobItem(
    @SerialName("id") val id: Int? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("primary_details") val primary_details: PrimaryDetails? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("requirements") val requirements: List<String>? = null,
    @SerialName("custom_link") val custom_link: String? = null
)

@Serializable
data class PrimaryDetails(
    @SerialName("Place") val Place: String? = null,
    @SerialName("Salary") val Salary: String? = null
)