package com.urdictionary.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.urdictionary.data.AstroSymbol
import com.urdictionary.data.DictionarySource
import com.urdictionary.data.InterpretationRow
import com.urdictionary.data.SearchMode
import com.urdictionary.data.SymbolCategory
import com.urdictionary.data.repository.FavoriteRow
import com.urdictionary.ui.theme.UrGold
import com.urdictionary.ui.theme.UrPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun URDictionaryScreen(
    windowSizeClass: WindowSizeClass,
    vm: URDictionaryViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    // Toast
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.toastMsg) {
        state.toastMsg?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            vm.clearToast()
        }
    }

    when (state.activeScreen) {
        AppScreen.MAIN     -> MainScreen(state, vm, isExpanded, snackbarHostState)
        AppScreen.SEARCH   -> SearchScreen(state, vm, snackbarHostState)
        AppScreen.FAVORITES-> FavoritesScreen(state, vm, snackbarHostState)
    }
}

// ─── Main Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    state: URUiState,
    vm: URDictionaryViewModel,
    isExpanded: Boolean,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            URTopAppBar(
                activeSource = state.activeSource,
                onSourceSelected = vm::onSourceSelected,
                onNavigateSearch = { vm.navigate(AppScreen.SEARCH) },
                onNavigateFavorites = { vm.navigate(AppScreen.FAVORITES) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (isExpanded) {
            Row(Modifier.fillMaxSize().padding(pad)) {
                // Left: symbol grid + controls
                Column(Modifier.weight(1f)) {
                    SymbolGridPanel(state, vm, minCell = 80.dp)
                }
                Divider(
                    modifier = Modifier.fillMaxHeight().width(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
                // Right: results
                Column(Modifier.weight(1f).padding(16.dp)) {
                    ResultPanel(state, vm, Modifier.fillMaxSize())
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(pad)) {
                SymbolGridPanel(state, vm, minCell = 66.dp, modifier = Modifier.weight(1f, fill = false))
                AnimatedVisibility(
                    visible = state.results.isNotEmpty() || state.isQuerying,
                    enter = fadeIn() + slideInVertically { it / 3 },
                    exit  = fadeOut()
                ) {
                    ResultPanel(state, vm, Modifier.weight(1f).padding(horizontal = 10.dp))
                }
            }
        }
    }
}

// ─── TopAppBar ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun URTopAppBar(
    activeSource: DictionarySource,
    onSourceSelected: (DictionarySource) -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateFavorites: () -> Unit,
) {
    var showSourceMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Text("♅", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                Spacer(Modifier.width(8.dp))
                Text(
                    " UR_Dic",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            // Source selector chip
            Surface(
                onClick = { showSourceMenu = true },
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    activeSource.displayEn,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            DropdownMenu(showSourceMenu, { showSourceMenu = false }) {
                DictionarySource.values().forEach { src ->
                    DropdownMenuItem(
                        text = { Text(src.displayTh) },
                        onClick = { onSourceSelected(src); showSourceMenu = false },
                        leadingIcon = {
                            if (src.name == "SYMPHONY") Text("♪", fontSize = 16.sp)
                            else Text("📖", fontSize = 16.sp)
                        }
                    )
                }
            }
            IconButton(onClick = onNavigateSearch) {
                Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onNavigateFavorites) {
                Icon(Icons.Default.Favorite, "Favorites", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
        )
    )
}

// ─── Symbol Grid Panel ────────────────────────────────────────────────────────

@Composable
private fun SymbolGridPanel(
    state: URUiState,
    vm: URDictionaryViewModel,
    minCell: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        // Selected symbols display (mirrors the "pnt" EditText)
        SelectedBar(state.selectedSymbols, state.activeSource,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )

        // Symbol grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCell),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            items(vm.allSymbols, key = { it.code }) { symbol ->
                SymbolCard(
                    symbol = symbol,
                    isSelected = state.selectedSymbols.any { it.code == symbol.code },
                    onTap = { vm.onSymbolTap(symbol) }
                )
            }
        }

        // CLEAR / Author label / OK row (matches screenshot exactly)
        ActionRow(
            onClear = vm::onClear,
            onOk = vm::onOk,
            hasSelection = state.selectedSymbols.isNotEmpty(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

// ─── Selected bar ─────────────────────────────────────────────────────────────

@Composable
private fun SelectedBar(
    selected: List<AstroSymbol>,
    source: DictionarySource,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        // Source label pill (mirrors tvDic)
        Surface(
            color = when (source) {
                DictionarySource.SYMPHONY -> Color(0x9999FFFF)
                DictionarySource.RUTH_EN, DictionarySource.RUTH_TH -> Color(0xFFD4B546)
                else -> Color(0xFFD4D8D7)
            },
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                source.displayTh,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF222222),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // Symbol chips
        if (selected.isEmpty()) {
            Text(
                "เลือกดาว / จุดชะตา…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            selected.forEachIndexed { i, sym ->
                if (i > 0) {
                    Text(" + ", color = UrGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sym.unicode, fontSize = 18.sp)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            sym.code,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

// ─── Symbol card ─────────────────────────────────────────────────────────────

@Composable
private fun SymbolCard(
    symbol: AstroSymbol,
    isSelected: Boolean,
    onTap: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        if (isSelected) 0.91f else 1f,
        spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    val bg = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant

    val glyphColor = when {
        symbol.category == SymbolCategory.AXIS ->
            if (isSelected) Color(0xFFFFD54F) else Color(0xFFE53935)
        symbol.category == SymbolCategory.LUMINARY ->
            if (isSelected) Color(0xFFFFCC80) else Color(0xFFE91E63)
        symbol.category == SymbolCategory.TRANSNEPTUNIAN ->
            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary
        else ->
            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onTap()
            },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.elevatedCardElevation(if (isSelected) 8.dp else 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bg)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(symbol.unicode, fontSize = 30.sp, color = glyphColor, lineHeight = 34.sp)
            Text(
                symbol.code,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Action row: author label + CLEAR + OK ────────────────────────────────────

@Composable
private fun ActionRow(
    onClear: () -> Unit,
    onOk: () -> Unit,
    hasSelection: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        // Author label (mirrors original "อ.ประยูร พลอารีย์" gray box)
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(6.dp)
        ) {
            Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                Text(
                    "อ.ประยูร พลอารีย์",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // CLEAR
        FilledTonalButton(
            onClick = onClear,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(0.65f)
        ) { Text("CLEAR", fontWeight = FontWeight.Bold, fontSize = 13.sp) }

        // OK — pink
        Button(
            onClick = onOk,
            enabled = hasSelection,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UrPink,
                contentColor = Color.White,
                disabledContainerColor = UrPink.copy(alpha = 0.35f)
            ),
            modifier = Modifier.weight(0.65f)
        ) { Text("OK", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp) }
    }
}

// ─── Result Panel ─────────────────────────────────────────────────────────────

@Composable
private fun ResultPanel(
    state: URUiState,
    vm: URDictionaryViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (state.isQuerying) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (state.results.isEmpty()) {
            if (state.selectedSymbols.isNotEmpty()) {
                EmptyCard()
            }
            return@Column
        }

        Text(
            "ผลลัพธ์ ${state.results.size} รายการ",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(state.results, key = { it.displayKey + it.source.name }) { row ->
                ResultCard(row, onSave = { vm.saveFavorite(row) })
            }
        }
    }
}

@Composable
private fun ResultCard(row: InterpretationRow, onSave: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(5.dp)) {
                        Text(
                            row.displayKey,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Surface(
                        color = when (row.source) {
                            DictionarySource.SYMPHONY -> Color(0x3399FFFF)
                            DictionarySource.RUTH_EN, DictionarySource.RUTH_TH -> Color(0x33D4B546)
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            row.source.displayEn,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                IconButton(onClick = onSave, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = UrPink,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(Modifier.height(8.dp))
            Text(
                row.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
private fun EmptyCard() {
    ElevatedCard(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔭", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text("ไม่พบข้อมูลในพจนานุกรมนี้", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Search Screen (Dicsearch) ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(state: URUiState, vm: URDictionaryViewModel, snackbarHostState: SnackbarHostState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Engine", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { vm.navigate(AppScreen.MAIN) }) {
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(12.dp)) {

            // Source selector
            Text("พจนานุกรม", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DictionarySource.values().forEach { src ->
                    FilterChip(
                        selected = state.activeSource == src,
                        onClick = { vm.onSourceSelected(src) },
                        label = { Text(src.displayEn, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // AND / OR toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("โหมด:", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = state.searchMode == SearchMode.OR,
                    onCheckedChange = { vm.onSearchModeToggle() }
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.searchMode == SearchMode.OR) "OR" else "AND",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))

            // Search field + buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = vm::onSearchQueryChange,
                    placeholder = { Text("เช่น เงิน โชค แต่งงาน…") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Button(onClick = vm::onSearch, colors = ButtonDefaults.buttonColors(containerColor = UrPink)) {
                    Text("ค้นหา", color = Color.White)
                }
                FilledTonalButton(onClick = { vm.onSearchQueryChange("") }) {
                    Text("ล้าง")
                }
            }
            Spacer(Modifier.height(12.dp))

            // Results
            if (state.isSearching) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text("ผลลัพธ์ ${state.searchResults.size} รายการ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.searchResults, key = { it.displayKey + it.content.take(20) }) { row ->
                        SearchResultItem(row, isAlt = state.searchResults.indexOf(row) % 2 == 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(row: InterpretationRow, isAlt: Boolean) {
    val bg = if (isAlt) MaterialTheme.colorScheme.surfaceVariant
             else MaterialTheme.colorScheme.surface
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp)) {
            Text(
                row.displayKey,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(90.dp)
            )
            Text(
                row.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Favorites Screen ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreen(state: URUiState, vm: URDictionaryViewModel, snackbarHostState: SnackbarHostState) {
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("ยืนยัน") },
            text = { Text("ต้องการลบรายการโปรดทั้งหมด?") },
            confirmButton = {
                TextButton(onClick = { vm.deleteAllFavorites(); showDeleteAllDialog = false }) {
                    Text("ลบ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { vm.navigate(AppScreen.MAIN) }) {
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteAllDialog = true }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (state.favorites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("ยังไม่มีรายการโปรด", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.favorites, key = { it.content }) { fav ->
                    FavoriteItem(
                        fav = fav,
                        index = state.favorites.indexOf(fav),
                        onDelete = { vm.deleteFavorite(fav) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteItem(fav: FavoriteRow, index: Int, onDelete: () -> Unit) {
    val bg = when (fav.sourceTag) {
        "2"  -> Color(0x33D4B546)
        "3"  -> Color(0x3399FFFF)
        else -> if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                fav.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}
