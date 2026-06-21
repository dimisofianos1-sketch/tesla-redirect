package gr.posokanei4real.app.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import gr.posokanei4real.app.data.model.PriceEntry
import gr.posokanei4real.app.data.model.Supermarket
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val db: FirebaseFirestore,
) {
    /** Fetch all current prices from the flat `prices_current` collection. */
    suspend fun fetchAllCurrentPrices(): List<PriceEntry> {
        val snapshot = db.collection("prices_current").get().await()
        return snapshot.documents.mapNotNull { doc ->
            runCatching {
                val data = doc.data ?: return@mapNotNull null
                val supermarketId = data["supermarket_id"] as? String ?: return@mapNotNull null
                PriceEntry(
                    itemId = data["item_id"] as? String ?: return@mapNotNull null,
                    supermarketId = supermarketId,
                    supermarketName = Supermarket.MAP[supermarketId]?.name ?: supermarketId,
                    price = (data["price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                    isOnSale = data["is_on_sale"] as? Boolean ?: false,
                    originalPrice = (data["original_price"] as? Number)?.toDouble(),
                    productNameFound = data["product_name_found"] as? String,
                    productUrl = data["product_url"] as? String,
                    scrapedAt = data["scraped_at"] as? String ?: "",
                )
            }.getOrNull()
        }
    }

    /** Fetch last update metadata. Returns ISO-8601 string or null. */
    suspend fun fetchLastUpdateAt(): String? = runCatching {
        db.collection("metadata").document("last_update").get().await()
            .getString("updated_at")
    }.getOrNull()
}
