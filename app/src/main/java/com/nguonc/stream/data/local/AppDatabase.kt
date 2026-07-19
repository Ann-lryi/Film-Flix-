package com.nguonc.stream.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ---------- Entities ----------

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val slug: String,
    val name: String,
    val originName: String,
    val posterUrl: String,
    val thumbUrl: String,
    val year: Int,
    val quality: String,
    val episodeCurrent: String,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "watch_history")
data class HistoryEntity(
    @PrimaryKey val slug: String,
    val name: String,
    val posterUrl: String,
    val episodeSlug: String,
    val episodeName: String,
    val positionMs: Long,
    val updatedAt: Long = System.currentTimeMillis(),
)

// ---------- DAOs ----------

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE slug = :slug)")
    fun observeIsFavorite(slug: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE slug = :slug")
    suspend fun delete(slug: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE slug = :slug LIMIT 1")
    suspend fun get(slug: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HistoryEntity)

    @Query("DELETE FROM watch_history WHERE slug = :slug")
    suspend fun delete(slug: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

// ---------- Database ----------

@Database(
    entities = [FavoriteEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
}
