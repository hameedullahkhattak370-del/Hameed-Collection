package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.cart.CartManager
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.ui.components.AnnouncementBar
import com.example.ui.components.ProductCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintEmeraldContainer
import com.example.ui.theme.RegalGold
import com.example.ui.theme.RichGoldAccent
import com.example.ui.theme.WarmGoldContainer

import com.example.data.model.Category

@Composable
fun HomeScreen(
    products: List<Product>,
    categories: List<Category>,
    settings: StoreSettings,
    isLoading: Boolean,
    wishlist: List<String>,
    onProductClick: (Product) -> Unit,
    onFavoriteToggle: (Product) -> Unit,
    onNavigateToCatalog: (category: String?) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val activeCategories = categories.filter { it.isActive }
    val categoryChipNames = listOf("All") + activeCategories.map { it.name }
    var selectedCategory by remember { mutableStateOf("All") }

    val activeCategoryIds = activeCategories.map { it.id }.toSet()
    val activeCategoryNames = activeCategories.map { it.name.lowercase() }.toSet()

    // Hide archived/inactive products and products whose categories are inactive
    val activeProducts = products.filter { p ->
        !p.isArchived && p.isActive && p.isAvailable &&
        (p.categoryId.isBlank() || activeCategoryIds.contains(p.categoryId) || activeCategoryNames.contains(p.category.lowercase()))
    }
    val featuredProducts = activeProducts.filter { it.isFeatured }
    val newArrivals = activeProducts.filter { it.isNewArrival }
    val filteredCatalog = if (selectedCategory == "All") {
        activeProducts
    } else {
        val matchedCat = activeCategories.find { it.name.equals(selectedCategory, ignoreCase = true) }
        activeProducts.filter { p ->
            p.category.equals(selectedCategory, ignoreCase = true) ||
            (matchedCat != null && p.categoryId == matchedCat.id)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Announcement Bar
        AnnouncementBar(message = settings.announcement)

        if (isLoading && products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_screen_list"),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Hero Banner
                item {
                    HeroBanner(
                        onShopNowClick = { onNavigateToCatalog(null) }
                    )
                }

                // Category Chips Row
                item {
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Collections",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = DeepEmerald
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = RegalGold
                                ),
                                modifier = Modifier.clickable { onNavigateToCatalog(null) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoryChipNames.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) },
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
                    }
                }

                // Featured Showcase
                if (featuredProducts.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Featured Classics")
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            items(featuredProducts, key = { it.id }) { product ->
                                ProductCard(
                                    product = product,
                                    isFavorite = wishlist.contains(product.id),
                                    onProductClick = { onProductClick(product) },
                                    onFavoriteToggle = { onFavoriteToggle(product) },
                                    onQuickAddClick = {
                                        CartManager.addItem(
                                            product = product,
                                            size = product.sizes.firstOrNull() ?: "Standard",
                                            color = product.colors.firstOrNull() ?: "Default"
                                        )
                                    },
                                    modifier = Modifier.width(190.dp)
                                )
                            }
                        }
                    }
                }

                // New Arrivals
                if (newArrivals.isNotEmpty()) {
                    item {
                        SectionHeader(title = "New Arrivals")
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            items(newArrivals, key = { it.id }) { product ->
                                ProductCard(
                                    product = product,
                                    isFavorite = wishlist.contains(product.id),
                                    onProductClick = { onProductClick(product) },
                                    onFavoriteToggle = { onFavoriteToggle(product) },
                                    onQuickAddClick = {
                                        CartManager.addItem(
                                            product = product,
                                            size = product.sizes.firstOrNull() ?: "Standard",
                                            color = product.colors.firstOrNull() ?: "Default"
                                        )
                                    },
                                    modifier = Modifier.width(190.dp)
                                )
                            }
                        }
                    }
                }

                // Catalog Grid (2 columns)
                item {
                    SectionHeader(
                        title = if (selectedCategory == "All") "Explore Store" else "$selectedCategory Collection"
                    )
                }

                if (filteredCatalog.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = DeepEmerald,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No products found in $selectedCategory",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepEmerald
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Check back soon or explore other Pakistani apparel collections.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Chunked grid items for 2 columns
                val chunkedProducts = filteredCatalog.chunked(2)
                items(chunkedProducts) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
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

                // Trust Badges Banner
                item {
                    TrustGuaranteesCard(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(RegalGold)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            ),
            color = DeepEmerald
        )
    }
}

@Composable
private fun HeroBanner(onShopNowClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepEmerald),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DeepEmerald,
                            EmeraldGreen,
                            Color(0xFF144D2A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.75f)) {
                Surface(
                    color = RichGoldAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "NEW SEASON 2026",
                        color = RichGoldAccent,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Royal Boski & Executive Wash & Wear",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 26.sp,
                        fontFamily = FontFamily.Serif
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Handcrafted traditional elegance delivered across Pakistan with Cash on Delivery.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onShopNowClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RichGoldAccent,
                        contentColor = DeepEmerald
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Shop Collection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Decorative background emblem
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, RichGoldAccent.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_hameed),
                    contentDescription = null,
                    modifier = Modifier.size(75.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun TrustGuaranteesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarmGoldContainer.copy(alpha = 0.6f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hameed Collection Promise",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = DeepEmerald
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrustFeature(
                    icon = Icons.Default.LocalShipping,
                    title = "Nationwide COD",
                    desc = "Pay cash on delivery"
                )
                TrustFeature(
                    icon = Icons.Default.Shield,
                    title = "100% Authentic",
                    desc = "Guaranteed pure fabric"
                )
                TrustFeature(
                    icon = Icons.Default.Star,
                    title = "Direct Seller",
                    desc = "No middlemen markup"
                )
            }
        }
    }
}

@Composable
private fun TrustFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(95.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(EmeraldGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RichGoldAccent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = DeepEmerald
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
