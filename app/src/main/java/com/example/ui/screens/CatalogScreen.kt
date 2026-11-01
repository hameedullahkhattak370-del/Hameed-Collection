package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.cart.CartManager
import com.example.data.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RegalGold

import com.example.data.model.Category

enum class SortOrder(val label: String) {
    FEATURED("Featured"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    DISCOUNT("Highest Discount")
}

@Composable
fun CatalogScreen(
    products: List<Product>,
    categories: List<Category>,
    isLoading: Boolean,
    wishlist: List<String>,
    initialCategory: String?,
    onProductClick: (Product) -> Unit,
    onFavoriteToggle: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: "All") }
    var sortOrder by remember { mutableStateOf(SortOrder.FEATURED) }
    var inStockOnly by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val activeCategories = categories.filter { it.isActive }
    val categoryChipNames = listOf("All") + activeCategories.map { it.name }
    val activeCategoryIds = activeCategories.map { it.id }.toSet()
    val activeCategoryNames = activeCategories.map { it.name.lowercase() }.toSet()

    val filteredProducts = products
        .filterNot { it.isArchived || !it.isActive }
        .filter { p ->
            p.categoryId.isBlank() || activeCategoryIds.contains(p.categoryId) || activeCategoryNames.contains(p.category.lowercase())
        }
        .filter { product ->
            val matchesCategory = selectedCategory == "All" ||
                product.category.equals(selectedCategory, ignoreCase = true) ||
                activeCategories.find { it.name.equals(selectedCategory, ignoreCase = true) }?.id == product.categoryId
            val matchesSearch = searchQuery.isBlank() ||
                product.title.contains(searchQuery, ignoreCase = true) ||
                product.description.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)
            val matchesStock = !inStockOnly || (product.stock > 0 && product.isAvailable)
            matchesCategory && matchesSearch && matchesStock
        }
        .let { list ->
            when (sortOrder) {
                SortOrder.FEATURED -> list.sortedByDescending { it.isFeatured }
                SortOrder.PRICE_LOW_HIGH -> list.sortedBy { it.displayPrice }
                SortOrder.PRICE_HIGH_LOW -> list.sortedByDescending { it.displayPrice }
                SortOrder.DISCOUNT -> list.sortedByDescending { it.discountPercent }
            }
        }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Boski, Kurta, Waistcoat...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = EmeraldGreen
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Categories horizontal bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categoryChipNames.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) EmeraldGreen else CardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sort & Results Count Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filteredProducts.size} items available",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        AssistChip(
                            onClick = { sortMenuExpanded = true },
                            label = { Text(sortOrder.label, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = EmeraldGreen
                                )
                            }
                        )

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            SortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        sortOrder = order
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = inStockOnly,
                        onClick = { inStockOnly = !inStockOnly },
                        label = { Text("In Stock", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Product Grid
        if (isLoading && products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldGreen)
            }
        } else if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = CardBorder
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No products found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepEmerald
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing your search terms or category filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("catalog_products_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 95.dp, top = 4.dp)
            ) {
                val chunkedProducts = filteredProducts.chunked(2)
                items(chunkedProducts) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in rowItems) {
                            ProductCard(
                                product = item,
                                isFavorite = wishlist.contains(item.id),
                                onProductClick = { onProductClick(item) },
                                onFavoriteToggle = { onFavoriteToggle(item) },
                                onQuickAddClick = {
                                    CartManager.addItem(
                                        product = item,
                                        size = item.sizes.firstOrNull() ?: "Standard",
                                        color = item.colors.firstOrNull() ?: "Default"
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
