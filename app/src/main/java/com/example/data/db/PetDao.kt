package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommentEntity
import com.example.data.model.CommunityPostEntity
import com.example.data.model.GrowthRecordEntity
import com.example.data.model.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE isActive = 1 LIMIT 1")
    fun getActivePetFlow(): Flow<PetEntity?>

    @Query("SELECT * FROM pets ORDER BY id ASC")
    fun getAllPetsFlow(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun getPetById(id: Long): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity): Long

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Query("UPDATE pets SET isActive = 0")
    suspend fun deactivateAllPets()

    @Query("UPDATE pets SET isActive = 1 WHERE id = :id")
    suspend fun setActivePet(id: Long)

    @Query("DELETE FROM pets WHERE id = :id")
    suspend fun deletePet(id: Long)
}

@Dao
interface GrowthRecordDao {
    @Query("SELECT * FROM growth_records WHERE petId = :petId ORDER BY timestamp DESC")
    fun getGrowthRecordsFlow(petId: Long): Flow<List<GrowthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GrowthRecordEntity): Long

    @Query("DELETE FROM growth_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity): Long

    @Update
    suspend fun updatePost(post: CommunityPostEntity)

    @Query("UPDATE community_posts SET likesCount = likesCount + :delta, isLiked = :isLiked WHERE id = :postId")
    suspend fun updatePostLike(postId: Long, delta: Int, isLiked: Boolean)

    @Query("SELECT * FROM post_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPostFlow(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long
}
