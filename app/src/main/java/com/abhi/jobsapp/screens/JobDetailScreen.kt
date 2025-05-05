package com.abhi.jobsapp.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun JobDetailScreen(
    jobId: Int,
    viewModel: JobDetailViewModel = viewModel(factory = JobDetailViewModelFactory(jobId))
) {
    val state by viewModel.state

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        }
        state.job != null -> {
            JobDetailContent(job = state.job!!)
        }
    }
}

@Composable
fun JobDetailContent(job: JobItem) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
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


class JobDetailViewModel(jobId: Int) : ViewModel() {
    private val _state = mutableStateOf(JobDetailState())
    val state: State<JobDetailState> get() = _state

    init {
        loadJob(jobId)
    }

    private fun loadJob(jobId: Int) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val job = fetchJob(jobId)
                _state.value = _state.value.copy(
                    job = job,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load job details"
                )
            }
        }
    }

    private suspend fun fetchJob(jobId: Int): JobItem {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
        }
        return client.get("https://testapi.getlokalapp.com/common/jobs/$jobId").body()
    }
}

data class JobDetailState(
    val job: JobItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class JobDetailViewModelFactory(private val jobId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobDetailViewModel(jobId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}