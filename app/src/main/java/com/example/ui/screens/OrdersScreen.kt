package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.cart.CartManager
import com.example.data.cart.formatPkr
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.ui.components.HameedProductImage
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintEmeraldContainer
import com.example.ui.theme.RegalGold
import com.example.ui.theme.RichGoldAccent
import com.example.ui.theme.WarmGoldContainer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersScreen(
    orders: List<Order>,
    wishlistProducts: List<Product>,
    settings: StoreSettings,
    isLoading: Boolean,
    onCancelOrder: suspend (String) -> Result<Unit>,
    onProductClick: (Product) -> Unit,
    onRemoveFromWishlist: (Product) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Orders (${orders.size})", "Wishlist (${wishlistProducts.size})")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmeraldGreen
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            OrdersListView(
                orders = orders,
                settings = settings,
                isLoading = isLoading,
                onCancelOrder = onCancelOrder
            )
        } else {
            WishlistListView(
                wishlistProducts = wishlistProducts,
                onProductClick = onProductClick,
                onRemoveFromWishlist = onRemoveFromWishlist
            )
        }
    }
}

@Composable
private fun OrdersListView(
    orders: List<Order>,
    settings: StoreSettings,
    isLoading: Boolean,
    onCancelOrder: suspend (String) -> Result<Unit>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    if (isLoading && orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EmeraldGreen)
        }
    } else if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MintEmeraldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Orders Yet",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "You haven't placed any Cash on Delivery orders yet. Browse our collections to start shopping!",
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
                .testTag("customer_orders_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                OrderCard(
                    order = order,
                    onCopyTracking = { code ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Tracking Number", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Tracking number copied: $code", Toast.LENGTH_SHORT).show()
                    },
                    onContactSupport = {
                        val rawPhone = settings.whatsapp.ifBlank { "+923271825407" }
                        val cleanDigits = rawPhone.replace(Regex("[^0-9]"), "")
                        val targetPhone = if (cleanDigits.isNotBlank() && cleanDigits != "923001234567") cleanDigits else "923271825407"
                        val message = "Assalam-o-Alaikum! I would like to inquire about my Order #${order.orderNumber} from Hameed Collection."
                        val url = "https://wa.me/$targetPhone?text=${Uri.encode(message)}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    addCategory(Intent.CATEGORY_BROWSABLE)
                                }
                                context.startActivity(browserIntent)
                            } catch (_: Exception) {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+$targetPhone")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(dialIntent)
                            }
                        }
                    },
                    onCancelClick = {
                        orderToCancel = order
                    }
                )
            }
        }
    }

    if (orderToCancel != null) {
        AlertDialog(
            onDismissRequest = { orderToCancel = null },
            title = { Text("Cancel Order?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to cancel Order #${orderToCancel?.orderNumber}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val o = orderToCancel ?: return@Button
                        isCancelling = true
                        coroutineScope.launch {
                            val res = onCancelOrder(o.id)
                            isCancelling = false
                            orderToCancel = null
                            if (res.isFailure) {
                                Toast.makeText(context, "Failed to cancel order", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    enabled = !isCancelling
                ) {
                    Text("Yes, Cancel Order")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToCancel = null }) {
                    Text("Keep Order")
                }
            }
        )
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onCopyTracking: (String) -> Unit,
    onContactSupport: () -> Unit,
    onCancelClick: () -> Unit
) {
    val dateStr = order.createdAt?.toDate()?.let {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(it)
    } ?: "Recent"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order Number and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepEmerald
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Items List
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.title} (${item.selectedSize.ifBlank { "Standard" }})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatPkr(item.price * item.quantity),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Totals Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Payable (COD):",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = formatPkr(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delivery City and Address
            Text(
                text = "Ship to: ${order.customerName} - ${order.deliveryAddress}, ${order.city}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Courier and Tracking info if dispatched
            if (order.status == "Dispatched" || order.status == "Delivered") {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = WarmGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Courier: ${order.courierName.ifBlank { "TCS Express" }}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepEmerald
                                )
                                Text(
                                    text = "Tracking: ${order.trackingNumber.ifBlank { "Assigned" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldGreen
                                )
                            }
                        }

                        if (order.trackingNumber.isNotBlank()) {
                            IconButton(onClick = { onCopyTracking(order.trackingNumber) }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy tracking number",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (order.status == "Pending") {
                    OutlinedButton(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = onContactSupport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintEmeraldContainer,
                        contentColor = EmeraldGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Store Support", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WishlistListView(
    wishlistProducts: List<Product>,
    onProductClick: (Product) -> Unit,
    onRemoveFromWishlist: (Product) -> Unit
) {
    if (wishlistProducts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(WarmGoldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = RegalGold,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Wishlist is Empty",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap the heart icon on any product to save it to your wishlist for later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(wishlistProducts, key = { it.id }) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(product) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val thumbUrl = product.effectiveImageUrl
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            HameedProductImage(
                                imageUrl = thumbUrl,
                                contentDescription = product.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                fallbackCategory = product.category.ifBlank { "Hameed Collection" }
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = RegalGold
                            )
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatPkr(product.displayPrice),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(onClick = { onRemoveFromWishlist(product) }) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Remove from wishlist",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    CartManager.addItem(
                                        product = product,
                                        size = product.sizes.firstOrNull() ?: "Standard",
                                        color = product.colors.firstOrNull() ?: "Default"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldGreen,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
