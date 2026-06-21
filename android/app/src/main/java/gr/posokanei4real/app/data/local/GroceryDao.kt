package gr.posokanei4real.app.data.local

import androidx.room.*
import gr.posokanei4real.app.data.model.Category
import gr.posokanei4real.app.data.model.GroceryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {

    @Query("SELECT * FROM categories ORDER BY id")
    fun allCategories(): Flow<List<Category>>

    @Query("SELECT * FROM grocery_items WHERE categoryId = :catId ORDER BY nameEl")
    fun itemsByCategory(catId: String): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE nameEl LIKE '%' || :q || '%' ORDER BY nameEl")
    fun search(q: String): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE id = :id")
    suspend fun getById(id: String): GroceryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<GroceryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)
}
