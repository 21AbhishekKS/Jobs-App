package com.abhi.jobsapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_jobs")
data class JobEntity(
    @PrimaryKey val id: Int,
    val title: String?,
    val place: String?,
    val salary: String?,
    val description: String?,
    val customLink: String?
)
