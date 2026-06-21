package gr.posokanei4real.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.posokanei4real.app.data.model.Category
import gr.posokanei4real.app.data.model.GroceryItem
import gr.posokanei4real.app.data.model.PriceEntry
import gr.posokanei4real.app.data.repository.GroceryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: String? = null,
    val items: List<GroceryItem> = emptyList(),
    val bestPrices: Map<String, PriceEntry> = emptyMap(),
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val lastUpdated: String? = null,
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repo: GroceryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    private val selectedCategory = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch { repo.seedFromAssets() }

        repo.categories
            .onEach { cats ->
                _state.update { it.copy(categories = cats) }
                if (selectedCategory.value == null && cats.isNotEmpty()) {
                    selectedCategory.value = cats.first().id
                }
            }
            .launchIn(viewModelScope)

        selectedCategory
            .flatMapLatest { catId ->
                if (catId != null) repo.itemsByCategory(catId)
                else flowOf(emptyList())
            }
            .onEach { items -> _state.update { it.copy(items = items) } }
            .launchIn(viewModelScope)

        searchQuery
            .debounce(300)
            .flatMapLatest { q ->
                if (q.isBlank()) flowOf(emptyList())
                else repo.searchItems(q)
            }
            .onEach { results ->
                if (searchQuery.value.isNotBlank()) {
                    _state.update { it.copy(items = results) }
                }
            }
            .launchIn(viewModelScope)

        repo.bestPricePerItem()
            .onEach { entries ->
                _state.update { it.copy(bestPrices = entries.associateBy { e -> e.itemId }) }
            }
            .launchIn(viewModelScope)

        loadLastUpdated()
    }

    fun selectCategory(id: String) {
        searchQuery.value = ""
        selectedCategory.value = id
        _state.update { it.copy(selectedCategory = id, searchQuery = "") }
    }

    fun onSearch(query: String) {
        searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = repo.refreshPrices()
            result.fold(
                onSuccess = { count ->
                    _state.update {
                        it.copy(isRefreshing = false, errorMessage = null)
                    }
                    loadLastUpdated()
                },
                onFailure = { err ->
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = "Αποτυχία ανανέωσης: ${err.localizedMessage}",
                        )
                    }
                }
            )
        }
    }

    private fun loadLastUpdated() {
        viewModelScope.launch {
            val ts = repo.lastUpdateAt()
            _state.update { it.copy(lastUpdated = ts) }
        }
    }
}
