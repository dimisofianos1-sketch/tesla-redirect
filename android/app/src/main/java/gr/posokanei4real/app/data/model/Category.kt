package gr.posokanei4real.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val nameEl: String,
    val nameEn: String,
    val icon: String,
)
