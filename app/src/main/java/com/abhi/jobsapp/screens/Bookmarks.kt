package com.abhi.jobsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhi.jobsapp.model.JobsDatabase
import com.abhi.jobsapp.model.JobEntity
import com.abhi.jobsapp.viewModels.JobItem
import com.abhi.jobsapp.viewModels.JobsViewModel
import com.abhi.jobsapp.viewModels.PrimaryDetails

@Composable
fun Bookmarks(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val jobDao = remember {
        JobsDatabase.getDatabase(context).jobDao()
    }
    val viewModel: JobsViewModel = viewModel()

    var savedJobs by remember { mutableStateOf<List<JobEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        savedJobs = jobDao.getAllJobs()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(savedJobs.size) { index ->
            val entity = savedJobs[index]
            val jobItem = JobItem(
                id = entity.id,
                title = entity.title,
                primary_details = PrimaryDetails(
                    Place = entity.place,
                    Salary = entity.salary
                ),
                description = entity.description,
                custom_link = entity.customLink
            )
            JobCard(
                viewModel = viewModel,
                onClick = {},
                job = jobItem)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

