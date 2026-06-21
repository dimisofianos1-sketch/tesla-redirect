package gr.posokanei4real.app.data.model

import androidx.room.Entity

/**
 * Cached price for one item at one supermarket.
 * Composite PK avoids duplicates when refreshing.
 */
@Entity(
    tableName = "price_entries",
    primaryKeys = ["itemId", "supermarketId"],
)
data class PriceEntry(
    val itemId: String,
    val supermarketId: String,
    val supermarketName: String,
    val price: Double,
    val isOnSale: Boolean = false,
    val originalPrice: Double? = null,
    val productNameFound: String? = null,
    val productUrl: String? = null,
    val scrapedAt: String = "",
)
