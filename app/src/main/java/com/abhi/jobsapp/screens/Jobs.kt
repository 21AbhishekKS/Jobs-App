package com.abhi.jobsapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abhi.jobsapp.model.JobEntity
import com.abhi.jobsapp.navigation.Routes
import com.abhi.jobsapp.viewModels.JobItem
import com.abhi.jobsapp.viewModels.JobsViewModel


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
        modifier = Modifier,
        viewModel
    )
}

@Composable
private fun JobsContent(
    jobs: List<JobItem>,
    isLoading: Boolean,
    error: String?,
    onLoadMore: () -> Unit,
    onJobClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JobsViewModel
) {
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Server Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Server Busy (503)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "We're experiencing high traffic right now.\nPlease try again in a few moments.",
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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
                        onClick = { onJobClick(job.id ?: 0) },
                        viewModel = viewModel
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
    viewModel: JobsViewModel
) {
    val favoriteJobs by viewModel.favoriteJobs.collectAsState()
    val jobId = job.id ?: -1
    val isFavorite = favoriteJobs.contains(jobId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color(0xFFFFC107), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title ?: "No Title",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0D47A1)
                    )
                    Text(
                        text = job.primary_details?.Salary ?: "No Salary",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        //job.company_name ?:
                        text =  "Unknown Company",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.toggleFavorite(
                            JobEntity(
                                id = jobId,
                                title = job.title,
                                place = job.primary_details?.Place,
                                salary = job.primary_details?.Salary,
                                description = job.description,
                                customLink = job.custom_link
                            )
                        )
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    //job.vacancies ?:
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = job.primary_details?.Place ?: "No Location",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Share logic */ },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Share")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {  },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call HR")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call HR")
                }
            }
        }
    }
}





