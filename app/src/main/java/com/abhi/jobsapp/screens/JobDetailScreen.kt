import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun JobDetailScreen(
    jobId: Int,
    navController: NavController
) {
    var job by remember { mutableStateOf<JobItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .padding(horizontal = 16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                job != null -> {
                    JobDetailContent(job = job!!)
                }
                else -> {
                    Text(
                        text = "No job details found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }



@Composable
fun JobDetailContent(job: JobItem) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = job.title ?: "No Title",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.Black)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    text = "${"0"} vacancies",
                    color = Color(0xFF1565C0),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Work from home",
                    color = Color(0xFF6A1B9A),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = job.primary_details?.Place ?: "No Location",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Salary: ${job.primary_details?.Salary ?: "Not specified"}",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Description:",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.Black)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = job.description ?: "No description available",
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Requirements:",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.Black)
            )
            Spacer(modifier = Modifier.height(4.dp))
            job.requirements?.forEach { requirement ->
                Text(text = "• $requirement", color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Share logic */ },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Share", color = Color.Black)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {  },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call HR", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call HR", color = Color.Black)
                }
            }
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