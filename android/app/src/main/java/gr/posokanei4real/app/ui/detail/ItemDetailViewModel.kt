package gr.posokanei4real.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gr.posokanei4real.app.data.model.GroceryItem
import gr.posokanei4real.app.data.model.PriceEntry
import gr.posokanei4real.app.data.repository.GroceryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val item: GroceryItem? = null,
    val prices: List<PriceEntry> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val repo: GroceryRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val itemId: String = checkNotNull(savedState["itemId"])

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val item = repo.getItem(itemId)
            _state.update { it.copy(item = item, isLoading = false) }
        }
        repo.pricesForItem(itemId)
            .onEach { prices -> _state.update { it.copy(prices = prices) } }
            .launchIn(viewModelScope)
    }
}
