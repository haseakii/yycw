package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE dateKey = :dateKey ORDER BY rowid ASC")
    fun getDailyTasksFlow(dateKey: String): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks WHERE dateKey = :dateKey ORDER BY rowid ASC")
    suspend fun getDailyTasks(dateKey: String): List<DailyTaskEntity>

    @Query("SELECT * FROM daily_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): DailyTaskEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTasks(tasks: List<DailyTaskEntity>)

    @Update
    suspend fun updateTask(task: DailyTaskEntity)

    @Query("UPDATE daily_tasks SET currentProgress = MIN(targetProgress, currentProgress + :delta) WHERE id = :id AND dateKey = :dateKey")
    suspend fun incrementProgress(id: String, dateKey: String, delta: Int = 1)

    @Query("UPDATE daily_tasks SET isClaimed = 1 WHERE id = :id")
    suspend fun markTaskClaimed(id: String)
}
