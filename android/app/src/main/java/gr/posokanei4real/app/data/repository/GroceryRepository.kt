package gr.posokanei4real.app.data.repository

import android.content.Context
import gr.posokanei4real.app.data.firebase.FirestoreService
import gr.posokanei4real.app.data.local.GroceryDao
import gr.posokanei4real.app.data.local.PriceDao
import gr.posokanei4real.app.data.model.Category
import gr.posokanei4real.app.data.model.GroceryItem
import gr.posokanei4real.app.data.model.PriceEntry
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroceryRepository @Inject constructor(
    private val groceryDao: GroceryDao,
    private val priceDao: PriceDao,
    private val firestoreService: FirestoreService,
    private val context: Context,
) {
    val categories: Flow<List<Category>> = groceryDao.allCategories()

    fun itemsByCategory(categoryId: String): Flow<List<GroceryItem>> =
        groceryDao.itemsByCategory(categoryId)

    fun searchItems(query: String): Flow<List<GroceryItem>> =
        groceryDao.search(query.trim())

    fun pricesForItem(itemId: String): Flow<List<PriceEntry>> =
        priceDao.pricesForItem(itemId)

    fun bestPricePerItem(): Flow<List<PriceEntry>> =
        priceDao.bestPricePerItem()

    suspend fun getItem(id: String): GroceryItem? = groceryDao.getById(id)

    /** Seed the local Room database from bundled assets/catalog.json on first run. */
    suspend fun seedFromAssets() {
        val json = context.assets.open("catalog.json").bufferedReader().readText()
        val root = JSONObject(json)
        val categories = mutableListOf<Category>()
        val items = mutableListOf<GroceryItem>()
        val cats = root.getJSONArray("categories")
        for (i in 0 until cats.length()) {
            val cat = cats.getJSONObject(i)
            categories += Category(
                id = cat.getString("id"),
                nameEl = cat.getString("name_el"),
                nameEn = cat.getString("name_en"),
                icon = cat.getString("icon"),
            )
            val catItems = cat.getJSONArray("items")
            for (j in 0 until catItems.length()) {
                val item = catItems.getJSONObject(j)
                items += GroceryItem(
                    id = item.getString("id"),
                    nameEl = item.getString("name_el"),
                    unit = item.getString("unit"),
                    categoryId = cat.getString("id"),
                )
            }
        }
        groceryDao.insertCategories(categories)
        groceryDao.insertItems(items)
    }

    /** Pull fresh prices from Firestore and cache them locally. */
    suspend fun refreshPrices(): Result<Int> = runCatching {
        val entries: List<PriceEntry> = firestoreService.fetchAllCurrentPrices()
        priceDao.upsert(entries)
        entries.size
    }

    suspend fun lastUpdateAt(): String? = firestoreService.fetchLastUpdateAt()
}
