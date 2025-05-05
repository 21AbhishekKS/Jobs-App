package com.abhi.jobsapp.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class JobsViewModel : ViewModel() {
    private val _jobs = mutableStateListOf<JobItem>()
    val jobs: List<JobItem> = _jobs

    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean get() = _isLoading.value

    private val _error = mutableStateOf<String?>(null)
    val error: String? get() = _error.value

    private var currentPage = 1
    private var hasMorePages = true

    init {
        loadJobs()
    }

    fun loadJobs() {
        if (_isLoading.value || !hasMorePages) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = fetchJobs(currentPage)
                _jobs.addAll(response.results)
                currentPage++
                hasMorePages = response.results.isNotEmpty()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load jobs"
            } finally {
                _isLoading.value = false
            }
        }
    }



    private suspend fun fetchJobs(page: Int): JobResponse {
        val url = "https://testapi.getlokalapp.com/common/jobs?page=$page"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return client.get(url).body()
    }
}

data class JobResponse(
    val results: List<JobItem>
)

data class JobItem(
    val id: Int,
    val title: String,
    val primary_details: PrimaryDetails,
    val custom_link: String
)

data class PrimaryDetails(
    val Place: String,
    val Salary: String
)