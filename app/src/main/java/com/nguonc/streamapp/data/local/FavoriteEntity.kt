package com.nguonc.streamapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val slug: String,
    val name: String,
    val originName: String,
    val thumbUrl: String,
    val posterUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)
