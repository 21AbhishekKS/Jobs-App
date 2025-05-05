package com.abhi.jobsapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abhi.jobsapp.navigation.Routes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Composable
fun Jobs(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: JobsViewModel = viewModel()
    val state = viewModel.state

    JobsContent(
        jobs = state.jobs,
        isLoading = state.isLoading,
        error = state.error,
        onLoadMore = { viewModel.loadJobs() },
        onJobClick = { jobId ->
            navController.navigate(Routes.JobDetail.createRoute(jobId))
        },
        modifier = Modifier
    )
}

@Composable
private fun JobsContent(
    jobs: List<JobItem>,
    isLoading: Boolean,
    error: String?,
    onLoadMore: () -> Unit,
    onJobClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize()) {
        if (error != null) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
            }
        }

        if (jobs.isEmpty() && isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(jobs) { job ->
                    JobCard(
                        job = job,
                        onClick = { onJobClick(job.id ?: 0) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= jobs.size - 1 && !isLoading
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }
}

@Composable
fun JobCard(
    job: JobItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = job.title ?: "No Title", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location: ${job.primary_details?.Place ?: "Not specified"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Salary: ${job.primary_details?.Salary ?: "Not specified"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class JobsState(
    val jobs: List<JobItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class JobsViewModel : ViewModel() {
    private val _state = mutableStateOf(JobsState())
    val state: JobsState get() = _state.value

    private var currentPage = 1
    private var hasMorePages = true

    init {
        loadJobs()
    }

    fun loadJobs() {
        if (_state.value.isLoading || !hasMorePages) return

        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val response = fetchJobs(currentPage)
                _state.value = _state.value.copy(
                    jobs = _state.value.jobs + response.results,
                    isLoading = false
                )
                currentPage++
                hasMorePages = response.results.isNotEmpty()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load jobs"
                )
            }
        }
    }

    private suspend fun fetchJobs(page: Int): JobResponse {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
        }
        return client.get("https://testapi.getlokalapp.com/common/jobs?page=$page").body()
    }
}

@Serializable
data class JobResponse(
    @SerialName("results") val results: List<JobItem> = emptyList()
)

@Serializable
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