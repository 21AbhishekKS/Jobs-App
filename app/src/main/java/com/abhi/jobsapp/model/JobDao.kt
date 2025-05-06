package com.abhi.jobsapp.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Query("SELECT * FROM favorite_jobs")
    suspend fun getAllJobs(): List<JobEntity>

    @Query("DELETE FROM favorite_jobs WHERE id = :jobId")
    suspend fun deleteJobById(jobId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_jobs WHERE id = :jobId)")
    suspend fun isJobFavorite(jobId: Int): Boolean

}
