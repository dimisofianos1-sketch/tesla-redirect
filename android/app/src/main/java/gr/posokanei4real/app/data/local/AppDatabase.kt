package gr.posokanei4real.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import gr.posokanei4real.app.data.model.Category
import gr.posokanei4real.app.data.model.GroceryItem
import gr.posokanei4real.app.data.model.PriceEntry

@Database(
    entities = [GroceryItem::class, Category::class, PriceEntry::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groceryDao(): GroceryDao
    abstract fun priceDao(): PriceDao
}
