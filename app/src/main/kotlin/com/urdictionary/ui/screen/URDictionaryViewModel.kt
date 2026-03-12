package com.urdictionary.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdictionary.data.ALL_SYMBOLS
import com.urdictionary.data.AstroSymbol
import com.urdictionary.data.DictionarySource
import com.urdictionary.data.InterpretationRow
import com.urdictionary.data.SearchMode
import com.urdictionary.data.repository.FavoriteRow
import com.urdictionary.data.repository.URDictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

data class URUiState(
    // Symbol grid
    val selectedSymbols: List<AstroSymbol> = emptyList(),   // max 2

    // Active dictionary source (mirrors context menu in original)
    val activeSource: DictionarySource = DictionarySource.COMBINE,

    // Main result list
    val results: List<InterpretationRow> = emptyList(),
    val isQuerying: Boolean = false,

    // Keyword search (Dicsearch screen)
    val searchQuery: String = "",
    val searchMode: SearchMode = SearchMode.AND,
    val searchResults: List<InterpretationRow> = emptyList(),
    val isSearching: Boolean = false,

    // Favorites
    val favorites: List<FavoriteRow> = emptyList(),
    val isFavoritesOpen: Boolean = false,

    // Other UI
    val activeScreen: AppScreen = AppScreen.MAIN,
    val toastMsg: String? = null,
)

enum class AppScreen { MAIN, SEARCH, FAVORITES }

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class URDictionaryViewModel @Inject constructor(
    private val repository: URDictionaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(URUiState())
    val state: StateFlow<URUiState> = _state.asStateFlow()

    val allSymbols: List<AstroSymbol> = ALL_SYMBOLS

    // ── Symbol selection ──────────────────────────────────────────────────────
    //
    // Original logic: ct counter < 2, each per-planet counter <= 2.
    // Max 2 symbols selected. Tapping a 3rd does nothing (original app).
    // CLEAR resets all counters and the text field.

    fun onSymbolTap(symbol: AstroSymbol) {
        _state.update { s ->
            val current = s.selectedSymbols.toMutableList()
            if (current.any { it.code == symbol.code }) {
                // Already selected — deselect it (extra UX, original had no deselect)
                current.removeAll { it.code == symbol.code }
            } else if (current.size < 2) {
                current.add(symbol)
            }
            // If already 2 selected, ignore (matches original ct < 2 guard)
            s.copy(selectedSymbols = current, results = emptyList())
        }
    }

    fun onClear() {
        _state.update { it.copy(selectedSymbols = emptyList(), results = emptyList()) }
    }

    fun onOk() {
        val codes = _state.value.selectedSymbols.map { it.code }
        if (codes.isEmpty()) {
            _state.update { it.copy(toastMsg = "Please assign the Planet") }
            return
        }
        val source = _state.value.activeSource
        _state.update { it.copy(isQuerying = true, results = emptyList()) }
        viewModelScope.launch {
            val rows = repository.query(codes, source)
            _state.update { it.copy(isQuerying = false, results = rows) }
        }
    }

    // ── Source selector ───────────────────────────────────────────────────────

    fun onSourceSelected(source: DictionarySource) {
        _state.update { it.copy(activeSource = source, results = emptyList()) }
    }

    // ── Keyword search ────────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onSearchModeToggle() {
        _state.update { s ->
            s.copy(searchMode = if (s.searchMode == SearchMode.AND) SearchMode.OR else SearchMode.AND)
        }
    }

    fun onSearch() {
        val q = _state.value.searchQuery.trim()
        if (q.isBlank()) {
            _state.update { it.copy(toastMsg = "กรุณาป้อนคำที่ต้องการค้นหา") }
            return
        }
        val source = _state.value.activeSource
        val mode = _state.value.searchMode
        _state.update { it.copy(isSearching = true, searchResults = emptyList()) }
        viewModelScope.launch {
            val rows = repository.keywordSearch(q, source, mode)
            _state.update { it.copy(isSearching = false, searchResults = rows) }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    fun navigate(screen: AppScreen) {
        _state.update { it.copy(activeScreen = screen) }
        if (screen == AppScreen.FAVORITES) loadFavorites()
    }

    // ── Favorites ─────────────────────────────────────────────────────────────

    fun loadFavorites() {
        viewModelScope.launch {
            val favs = repository.getFavorites()
            _state.update { it.copy(favorites = favs) }
        }
    }

    fun saveFavorite(row: InterpretationRow) {
        val tag = when (row.source) {
            DictionarySource.COMBINE  -> "1"
            DictionarySource.RUTH_EN  -> "2"
            DictionarySource.SYMPHONY -> "3"
            else                      -> "1"
        }
        viewModelScope.launch {
            repository.saveFavorite(tag, "${row.displayKey} ${row.content}")
            _state.update { it.copy(toastMsg = "บันทึกสำเร็จ") }
        }
    }

    fun deleteFavorite(fav: FavoriteRow) {
        viewModelScope.launch {
            repository.deleteFavorite(fav.content)
            loadFavorites()
            _state.update { it.copy(toastMsg = "ลบแล้ว") }
        }
    }

    fun deleteAllFavorites() {
        viewModelScope.launch {
            repository.deleteAllFavorites()
            _state.update { it.copy(favorites = emptyList(), toastMsg = "ลบทั้งหมดแล้ว") }
        }
    }

    fun clearToast() {
        _state.update { it.copy(toastMsg = null) }
    }
}
