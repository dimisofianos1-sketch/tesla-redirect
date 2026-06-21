package gr.posokanei4real.app.data.local

import androidx.room.*
import gr.posokanei4real.app.data.model.PriceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {

    @Query("SELECT * FROM price_entries WHERE itemId = :itemId ORDER BY price ASC")
    fun pricesForItem(itemId: String): Flow<List<PriceEntry>>

    @Query(
        """
        SELECT pe.* FROM price_entries pe
        INNER JOIN (
            SELECT itemId, MIN(price) AS min_price
            FROM price_entries
            GROUP BY itemId
        ) best ON pe.itemId = best.itemId AND pe.price = best.min_price
        """
    )
    fun bestPricePerItem(): Flow<List<PriceEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entries: List<PriceEntry>)

    @Query("DELETE FROM price_entries")
    suspend fun clear()
}
