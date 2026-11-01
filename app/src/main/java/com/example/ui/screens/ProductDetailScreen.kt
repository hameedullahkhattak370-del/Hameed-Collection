package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.cart.CartManager
import com.example.data.cart.formatPkr
import com.example.data.model.Product
import com.example.ui.components.HameedProductImage
import com.example.ui.components.ProductCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintEmeraldContainer
import com.example.ui.theme.RegalGold
import com.example.ui.theme.RichGoldAccent
import com.example.ui.theme.WarmGoldContainer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    allProducts: List<Product> = emptyList(),
    isFavorite: Boolean,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit = {},
    onFavoriteToggle: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val cartItems by CartManager.items.collectAsState()
    val cartItemCount = cartItems.sumOf { it.quantity }

    var selectedSize by remember(product.id) {
        mutableStateOf(product.sizes.firstOrNull() ?: "4.25 Meters (Standard)")
    }
    var selectedColor by remember(product.id) {
        mutableStateOf(product.colors.firstOrNull() ?: "Default")
    }
    var quantity by remember(product.id) { mutableIntStateOf(1) }
    var selectedHeroIndex by remember(product.id) { mutableIntStateOf(0) }
    val isPurchasable = product.stock > 0 && product.isAvailable

    // Consolidate all product images (main cover image + additional gallery images)
    val allImages = remember(product.id, product.imageUrl, product.images) {
        buildList {
            val main = product.imageUrl.trim()
            if (main.isNotBlank()) add(main)
            product.images.forEach { img ->
                val trimmed = img.trim()
                if (trimmed.isNotBlank() && !contains(trimmed)) {
                    add(trimmed)
                }
            }
            if (isEmpty() && product.effectiveImageUrl.isNotBlank()) {
                add(product.effectiveImageUrl.trim())
            }
        }
    }

    // Additional images for the Temu-style vertical gallery:
    // If only one image exists, show that image.
    // If multiple images exist, show the additional ones without repeating the top hero image.
    // If no images exist, hide gracefully.
    val verticalGalleryImages = remember(allImages) {
        when {
            allImages.size > 1 -> allImages.drop(1)
            allImages.size == 1 -> allImages
            else -> emptyList()
        }
    }

    // Dynamic Similar Products: use allProducts if provided, else query Firestore
    var dynamicSimilarProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    LaunchedEffect(product.id, allProducts.size) {
        if (allProducts.isEmpty()) {
            try {
                val db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
                val snapshot = db.collection("products")
                    .whereEqualTo("isActive", true)
                    .limit(20)
                    .get()
                    .await()
                val list = snapshot.documents.mapNotNull { doc ->
                    val p = doc.toObject(Product::class.java)
                    p?.copy(id = if (p.id.isNotBlank()) p.id else doc.id)
                }
                dynamicSimilarProducts = list.filter { it.id != product.id && it.isAvailable }
            } catch (_: Exception) {
                // Ignore silent fallback
            }
        }
    }

    val similarProducts = remember(product.id, allProducts, dynamicSimilarProducts) {
        val pool = if (allProducts.isNotEmpty()) allProducts else dynamicSimilarProducts
        val validPool = pool.filter { it.id != product.id && it.isActive && it.isAvailable }

        // Prioritize products with matching category
        val sameCategory = validPool.filter {
            (it.categoryId.isNotBlank() && it.categoryId == product.categoryId) ||
            it.category.equals(product.category, ignoreCase = true)
        }
        val otherCategory = validPool.filter {
            !((it.categoryId.isNotBlank() && it.categoryId == product.categoryId) ||
              it.category.equals(product.category, ignoreCase = true))
        }

        (sameCategory + otherCategory).distinctBy { it.id }.take(8)
    }

    // Full screen zoomable image viewer state
    var fullScreenViewerIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Sticky Temu-style Bottom Action Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Cart Button with Badge
                    IconButton(
                        onClick = onNavigateToCart,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFB91C1C),
                                        contentColor = Color.White
                                    ) {
                                        Text("$cartItemCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "View Cart",
                                tint = DeepEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Outlined Add to Cart Button
                    OutlinedButton(
                        onClick = {
                            CartManager.addItem(
                                product = product,
                                size = selectedSize,
                                color = selectedColor,
                                quantity = quantity
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Added $quantity item(s) to your cart")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_to_cart_button"),
                        enabled = isPurchasable
                    ) {
                        Icon(
                            Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            tint = if (isPurchasable) EmeraldGreen else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!product.isAvailable) "Unavailable"
                                   else if (product.stock <= 0) "Out of Stock"
                                   else "Add to Cart",
                            color = if (isPurchasable) EmeraldGreen else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Primary Buy with COD Button (Temu Style)
                    Button(
                        onClick = {
                            CartManager.addItem(
                                product = product,
                                size = selectedSize,
                                color = selectedColor,
                                quantity = quantity
                            )
                            onNavigateToCart()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("buy_now_cod_button"),
                        enabled = isPurchasable
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isPurchasable) "Buy with COD" else "Sold Out",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (isPurchasable) {
                                Text(
                                    text = formatPkr(product.displayPrice * quantity),
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. HERO IMAGE CAROUSEL & TOP TOOLBAR
            val currentHeroImage = if (allImages.isNotEmpty()) {
                allImages[selectedHeroIndex.coerceIn(0, allImages.size - 1)]
            } else ""

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Hero Image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            if (allImages.isNotEmpty()) {
                                fullScreenViewerIndex = selectedHeroIndex.coerceIn(0, allImages.size - 1)
                            }
                        }
                ) {
                    HameedProductImage(
                        imageUrl = currentHeroImage,
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        fallbackCategory = product.category.ifBlank { "Hameed Collection" }
                    )
                }

                // Overlay if unavailable or out of stock
                if (!product.isAvailable || product.stock <= 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = if (!product.isAvailable) Color(0xFFDC2626) else Color(0xFFB91C1C),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (!product.isAvailable) "CURRENTLY UNAVAILABLE" else "OUT OF STOCK",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Temu-style Image Counter Pill (Bottom Right)
                if (allImages.isNotEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${selectedHeroIndex + 1} / ${allImages.size}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Tap to zoom hint (Bottom Left)
                Surface(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Tap image to zoom",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Top Floating Toolbar (Glassmorphic Controls)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out this ${product.title} (${product.category}) for ${formatPkr(product.displayPrice)} on Hameed Collection Pakistan!"
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Product via")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = onFavoriteToggle,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFEF4444) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Thumbnail Strip (if multiple photos)
            if (allImages.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(allImages) { idx, imgUrl ->
                        val isSelected = idx == selectedHeroIndex
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) EmeraldGreen else CardBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedHeroIndex = idx }
                        ) {
                            HameedProductImage(
                                imageUrl = imgUrl,
                                contentDescription = "Thumbnail $idx",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                fallbackCategory = product.category.ifBlank { "Hameed Collection" }
                            )
                        }
                    }
                }
            }

            // 2. PRODUCT DETAILS, PRICE & SELECTION SECTION
            Column(modifier = Modifier.padding(16.dp)) {
                // Category & In-Stock Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MintEmeraldContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = product.category.ifBlank { "Apparel" }.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepEmerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = if (!product.isAvailable) MaterialTheme.colorScheme.errorContainer
                                else if (product.stock > 0) MintEmeraldContainer
                                else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (!product.isAvailable) "Unavailable"
                                   else if (product.stock > 0) "In Stock (${product.stock} units)"
                                   else "Out of Stock",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (product.isAvailable && product.stock > 0) EmeraldGreen else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Title
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = DeepEmerald
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Temu-Style Price Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatPkr(product.displayPrice),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = EmeraldGreen
                                )

                                if (product.hasDiscount) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatPkr(product.price),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (product.hasDiscount) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Lowest price in 30 days",
                                    fontSize = 11.sp,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (product.hasDiscount) {
                            Surface(
                                color = Color(0xFFB91C1C),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "-${product.discountPercent}% OFF",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Trust and Assurance Highlights (Temu style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarmGoldContainer.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash on Delivery", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepEmerald)
                        }
                        Text("•", color = RegalGold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("100% Authentic", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepEmerald)
                        }
                        Text("•", color = RegalGold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Doorstep Check", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepEmerald)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(14.dp))

                // Size Selector
                if (product.sizes.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Size / Length:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepEmerald
                        )
                        Text(
                            text = selectedSize,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        product.sizes.forEach { size ->
                            val isSelected = selectedSize == size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) EmeraldGreen else CardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(if (isSelected) MintEmeraldContainer else MaterialTheme.colorScheme.surface)
                                    .clickable { selectedSize = size }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = size,
                                    color = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Color Selector
                if (product.colors.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Color / Shade:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepEmerald
                        )
                        Text(
                            text = selectedColor,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        product.colors.forEach { colorName ->
                            val isSelected = selectedColor == colorName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) EmeraldGreen else CardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(if (isSelected) MintEmeraldContainer else MaterialTheme.colorScheme.surface)
                                    .clickable { selectedColor = colorName }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = colorName,
                                        color = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Quantity Selector Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepEmerald
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { if (quantity < product.stock) quantity++ },
                            modifier = Modifier.size(34.dp),
                            enabled = quantity < product.stock
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(14.dp))

                // Description & Fabric Details
                Text(
                    text = "Product Details & Fabric Care",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product.description.ifBlank { "Premium authentic Pakistani tailored unstitched fabric from Hameed Collection. Soft drape, unmatched comfort, and durable weave for all occasions." },
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 3. ADDITIONAL PRODUCT IMAGES GALLERY (Temu Style)
            // Immediately below product details and selection sections.
            if (verticalGalleryImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    // Gallery Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Product Images",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = DeepEmerald
                            )
                            Text(
                                text = "High-resolution fabric & styling gallery (${verticalGalleryImages.size} photos)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MintEmeraldContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tap to zoom", fontSize = 11.sp, color = DeepEmerald, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vertical Gallery List: each image large, proper aspect ratio, rounded corners
                    verticalGalleryImages.forEachIndexed { index, imageUrl ->
                        val targetViewerIndex = allImages.indexOf(imageUrl).let { if (it >= 0) it else 0 }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    fullScreenViewerIndex = targetViewerIndex
                                },
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                HameedProductImage(
                                    imageUrl = imageUrl,
                                    contentDescription = "${product.title} photo ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    fallbackCategory = product.category
                                )

                                // Photo index badge
                                Surface(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. SIMILAR PRODUCTS SECTION (Temu Style)
            // Immediately below the additional product images gallery.
            if (similarProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Section Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Similar Products You May Like",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = DeepEmerald
                            )
                            Text(
                                text = "Handpicked from ${product.category.ifBlank { "Hameed Collection" }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = RegalGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2-Column Responsive Grid for Similar Products
                    val chunked = similarProducts.chunked(2)
                    chunked.forEach { rowPair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (similarItem in rowPair) {
                                ProductCard(
                                    product = similarItem,
                                    isFavorite = false,
                                    onProductClick = {
                                        onProductClick(similarItem)
                                    },
                                    onFavoriteToggle = { /* handled via wishlist */ },
                                    onQuickAddClick = {
                                        CartManager.addItem(
                                            product = similarItem,
                                            size = similarItem.sizes.firstOrNull() ?: "Standard",
                                            color = similarItem.colors.firstOrNull() ?: "Default"
                                        )
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Added ${similarItem.title} to cart")
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 5. INTERACTIVE FULL-SCREEN ZOOMABLE IMAGE VIEWER (MODAL DIALOG)
    if (fullScreenViewerIndex != null && allImages.isNotEmpty()) {
        val initialIdx = fullScreenViewerIndex!!.coerceIn(0, allImages.size - 1)
        var currentViewerIndex by remember { mutableIntStateOf(initialIdx) }
        var scale by remember(currentViewerIndex) { mutableFloatStateOf(1f) }
        var offset by remember(currentViewerIndex) { mutableStateOf(Offset.Zero) }

        Dialog(
            onDismissRequest = { fullScreenViewerIndex = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { fullScreenViewerIndex = null },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    // Index indicator
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${currentViewerIndex + 1} / ${allImages.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val imgUrl = allImages.getOrNull(currentViewerIndex).orEmpty()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Look at this ${product.title} from Hameed Collection: $imgUrl")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Image"))
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }

                // Interactive Zoomable Main Image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(currentViewerIndex) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 4.5f)
                                scale = newScale
                                if (newScale > 1f) {
                                    val maxOffsetX = (size.width * (newScale - 1f)) / 2f
                                    val maxOffsetY = (size.height * (newScale - 1f)) / 2f
                                    val newOffset = offset + pan
                                    offset = Offset(
                                        x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                        y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        }
                        .pointerInput(currentViewerIndex) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val activeUrl = allImages.getOrNull(currentViewerIndex).orEmpty()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        HameedProductImage(
                            imageUrl = activeUrl,
                            contentDescription = "Zoomed Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            fallbackCategory = product.category
                        )
                    }
                }

                // Navigation Controls (Previous / Next Arrows)
                if (allImages.size > 1 && scale == 1f) {
                    if (currentViewerIndex > 0) {
                        IconButton(
                            onClick = {
                                currentViewerIndex--
                                scale = 1f
                                offset = Offset.Zero
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(12.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Previous Image", tint = Color.White)
                        }
                    }

                    if (currentViewerIndex < allImages.size - 1) {
                        IconButton(
                            onClick = {
                                currentViewerIndex++
                                scale = 1f
                                offset = Offset.Zero
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(12.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next Image", tint = Color.White)
                        }
                    }
                }

                // Bottom Hint & Thumbnail Indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pinch to zoom in/out • Double tap to toggle 2.5x",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    if (allImages.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            itemsIndexed(allImages) { idx, thumbUrl ->
                                val isSelected = idx == currentViewerIndex
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) EmeraldGreen else Color.White.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            currentViewerIndex = idx
                                            scale = 1f
                                            offset = Offset.Zero
                                        }
                                ) {
                                    HameedProductImage(
                                        imageUrl = thumbUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
