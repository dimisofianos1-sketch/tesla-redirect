package gr.posokanei4real.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey val id: String,
    val nameEl: String,
    val unit: String,
    val categoryId: String,
)
