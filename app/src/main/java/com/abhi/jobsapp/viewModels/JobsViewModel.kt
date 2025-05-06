package com.abhi.jobsapp.viewModels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhi.jobsapp.model.JobsDatabase
import com.abhi.jobsapp.model.JobEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class JobsState(
    val jobs: List<JobItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class JobsViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = mutableStateOf(JobsState())
    val state: JobsState get() = _state.value

    private var currentPage = 1
    private var hasMorePages = true

    private val db = JobsDatabase.getDatabase(application)
    private val jobDao = db.jobDao()


    private val dao = JobsDatabase.getDatabase(application).jobDao()

    private val _favoriteJobs = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteJobs = _favoriteJobs.asStateFlow()

    init {
        viewModelScope.launch {
            val jobs = dao.getAllJobs()
            _favoriteJobs.value = jobs.map { it.id }.toSet()
        }
    }

    fun toggleFavorite(job: JobEntity) {
        viewModelScope.launch {
            val isFavorite = dao.isJobFavorite(job.id)
            if (isFavorite) {
                dao.deleteJobById(job.id)
            } else {
                dao.insertJob(job)
            }
            // Refresh local favorite list
            val updated = dao.getAllJobs().map { it.id }.toSet()
            _favoriteJobs.value = updated
        }
    }



    fun saveJobToFavorites(job: JobItem) {
        viewModelScope.launch {
            val entity = JobEntity(
                id = job.id ?: return@launch,
                title = job.title,
                place = job.primary_details?.Place,
                salary = job.primary_details?.Salary,
                description = job.description,
                customLink = job.custom_link
            )
            jobDao.insertJob(entity)
        }
    }

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