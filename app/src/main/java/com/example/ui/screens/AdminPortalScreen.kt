package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.ImageStorageHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.HameedProductImage
import com.example.data.cart.formatPkr
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.StoreSettings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    orders: List<Order>,
    products: List<Product>,
    categories: List<com.example.data.model.Category>,
    settings: StoreSettings,
    onBack: () -> Unit,
    onAddProduct: suspend (Product) -> Result<String>,
    onUpdateProduct: suspend (Product) -> Result<Unit>,
    onDeleteProduct: suspend (String) -> Result<Unit>,
    onAddCategory: suspend (String, String, Boolean) -> Result<String>,
    onUpdateCategory: suspend (String, String, String, Boolean) -> Result<Unit>,
    onDeleteCategory: suspend (String) -> Result<Unit>,
    onUpdateOrderStatus: suspend (orderId: String, status: String, courier: String, tracking: String) -> Result<Unit>,
    onUpdateStoreSettings: suspend (StoreSettings) -> Result<Unit>,
    onSeedCatalog: suspend () -> Unit,
    onSeedCategories: suspend () -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Orders", "Products", "Categories", "Settings", "Overview")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Portal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Hameed Collection Store Manager",
                            style = MaterialTheme.typography.labelSmall,
                            color = RichGoldAccent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepEmerald
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("admin_portal_screen")
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepEmerald,
                contentColor = RichGoldAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RichGoldAccent
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
                                color = if (selectedTab == index) RichGoldAccent else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> AdminOrdersView(orders = orders, onUpdateStatus = onUpdateOrderStatus)
                1 -> AdminProductsView(
                    products = products,
                    categories = categories,
                    onAddProduct = onAddProduct,
                    onUpdateProduct = onUpdateProduct,
                    onDeleteProduct = onDeleteProduct,
                    onSeedCatalog = onSeedCatalog
                )
                2 -> AdminCategoriesView(
                    categories = categories,
                    products = products,
                    onAddCategory = onAddCategory,
                    onUpdateCategory = onUpdateCategory,
                    onDeleteCategory = onDeleteCategory,
                    onSeedCategories = onSeedCategories
                )
                3 -> AdminSettingsView(settings = settings, onUpdateSettings = onUpdateStoreSettings)
                4 -> AdminOverviewView(orders = orders, products = products)
            }
        }
    }
}

@Composable
private fun AdminOrdersView(
    orders: List<Order>,
    onUpdateStatus: suspend (String, String, String, String) -> Result<Unit>
) {
    var statusFilter by remember { mutableStateOf("All") }
    val statuses = listOf("All", "Pending", "Confirmed", "Packed", "Dispatched", "Delivered", "Cancelled")

    val filteredOrders = if (statusFilter == "All") {
        orders
    } else {
        orders.filter { it.status == statusFilter }
    }

    var orderToEdit by remember { mutableStateOf<Order?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statuses.forEach { st ->
                val isSelected = statusFilter == st
                val count = if (st == "All") orders.size else orders.count { it.status == st }
                FilterChip(
                    selected = isSelected,
                    onClick = { statusFilter = st },
                    label = { Text("$st ($count)", fontSize = 12.sp) }
                )
            }
        }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No orders under '$statusFilter'",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    AdminOrderCard(order = order, onEditClick = { orderToEdit = order })
                }
            }
        }
    }

    if (orderToEdit != null) {
        AdminOrderEditDialog(
            order = orderToEdit!!,
            onDismiss = { orderToEdit = null },
            onSave = onUpdateStatus
        )
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = order.orderNumber,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepEmerald
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (order.isGuest || order.customerId.startsWith("guest_")) WarmGoldContainer else MintEmeraldContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (order.isGuest || order.customerId.startsWith("guest_")) "GUEST" else "CUSTOMER",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = if (order.isGuest || order.customerId.startsWith("guest_")) RegalGold else EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${order.customerName} (${order.customerPhone})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            val fullAddress = if (order.area.isNotBlank()) {
                "${order.deliveryAddress}, ${order.area}, ${order.city}"
            } else {
                "${order.deliveryAddress}, ${order.city}"
            }
            Text(
                text = "Address: $fullAddress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (order.deliveryNotes.isNotBlank()) {
                Text(
                    text = "Notes: ${order.deliveryNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RegalGold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Items summary
            Text(
                text = "${order.items.size} item(s): " + order.items.joinToString(", ") { "${it.quantity}x ${it.title}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payable (COD): ${formatPkr(order.totalAmount)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )

                Button(
                    onClick = onEditClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Status", fontSize = 12.sp)
                }
            }

            if (order.courierName.isNotBlank() || order.trackingNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = WarmGoldContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Courier: ${order.courierName} | Tracking: ${order.trackingNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = DeepEmerald,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOrderEditDialog(
    order: Order,
    onDismiss: () -> Unit,
    onSave: suspend (String, String, String, String) -> Result<Unit>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val statusOptions = listOf("Pending", "Confirmed", "Packed", "Dispatched", "Delivered", "Cancelled", "Returned")

    var selectedStatus by remember { mutableStateOf(order.status) }
    var courierName by remember { mutableStateOf(order.courierName) }
    var trackingNumber by remember { mutableStateOf(order.trackingNumber) }
    var isSaving by remember { mutableStateOf(false) }

    val popularCouriers = listOf("TCS", "Leopards", "Trax", "PostEx", "M&P", "Call Courier")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Update Order #${order.orderNumber}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Customer: ${order.customerName} - ${order.customerPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Destination: ${order.deliveryAddress}, ${order.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Change Fulfillment Status",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.forEach { st ->
                        val isSelected = selectedStatus == st
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatus = st },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Courier Service",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularCouriers.forEach { courier ->
                        val isSelected = courierName.equals(courier, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { courierName = courier },
                            label = { Text(courier, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = trackingNumber,
                    onValueChange = { trackingNumber = it },
                    label = { Text("Tracking Number / Consignment #") },
                    placeholder = { Text("e.g. TCS789654321") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    coroutineScope.launch {
                        val res = onSave(order.id, selectedStatus, courierName, trackingNumber)
                        isSaving = false
                        if (res.isSuccess) {
                            Toast.makeText(context, "Order updated successfully", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Failed to update order", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color.White
                ),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = RichGoldAccent, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AdminProductsView(
    products: List<Product>,
    categories: List<com.example.data.model.Category>,
    onAddProduct: suspend (Product) -> Result<String>,
    onUpdateProduct: suspend (Product) -> Result<Unit>,
    onDeleteProduct: suspend (String) -> Result<Unit>,
    onSeedCatalog: suspend () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar with Add Product & Seed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isAddingNewProduct = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Product", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        onSeedCatalog()
                        Toast.makeText(context, "Seeded authentic Pakistani collection", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Seed Demo", fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product image thumbnail
                        val thumbUrl = product.effectiveImageUrl
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            HameedProductImage(
                                imageUrl = thumbUrl,
                                contentDescription = product.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                fallbackCategory = product.category.ifBlank { "Hameed Collection" }
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = product.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (product.isArchived) {
                                    Surface(
                                        color = Color(0xFF6B7280),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ARCHIVED",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (!product.isAvailable) {
                                    Surface(
                                        color = Color(0xFFDC2626),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "UNAVAILABLE",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${product.category} | Price: ${formatPkr(product.price)} | Sale: ${formatPkr(product.displayPrice)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Stock: ${product.stock} units | Sizes: ${product.sizes.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (product.stock <= 5 || !product.isAvailable) Color(0xFFB91C1C) else EmeraldGreen
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val updated = product.copy(isArchived = !product.isArchived)
                                        onUpdateProduct(updated)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (product.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                    contentDescription = if (product.isArchived) "Unarchive" else "Archive",
                                    tint = RegalGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(onClick = { productToEdit = product }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit product",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(onClick = { productToDelete = product }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete product",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Product Dialog
    if (isAddingNewProduct || productToEdit != null) {
        val editing = productToEdit
        AdminProductFormDialog(
            initialProduct = editing,
            categories = categories,
            onDismiss = {
                isAddingNewProduct = false
                productToEdit = null
            },
            onSave = { prod ->
                coroutineScope.launch {
                    val result = if (editing != null) {
                        onUpdateProduct(prod)
                    } else {
                        onAddProduct(prod).map { }
                    }
                    if (result.isSuccess) {
                        Toast.makeText(context, "Product saved successfully", Toast.LENGTH_SHORT).show()
                        isAddingNewProduct = false
                        productToEdit = null
                    } else {
                        Toast.makeText(context, "Failed to save product", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val toDelete = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete '${toDelete.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val res = onDeleteProduct(toDelete.id)
                            productToDelete = null
                            if (res.isSuccess) {
                                Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminProductFormDialog(
    initialProduct: Product?,
    categories: List<com.example.data.model.Category>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(initialProduct?.title ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var categoryId by remember {
        mutableStateOf(
            initialProduct?.categoryId?.ifBlank { null }
                ?: categories.find { it.name.equals(initialProduct?.category, ignoreCase = true) }?.id
                ?: categories.firstOrNull()?.id.orEmpty()
        )
    }
    var category by remember {
        mutableStateOf(
            initialProduct?.category?.ifBlank { null }
                ?: categories.find { it.id == categoryId }?.name
                ?: categories.firstOrNull()?.name.orEmpty()
        )
    }
    var priceStr by remember { mutableStateOf(initialProduct?.price?.toString() ?: "5000") }
    var salePriceStr by remember { mutableStateOf(initialProduct?.salePrice?.toString() ?: "0") }
    var stockStr by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "20") }
    var sizesStr by remember { mutableStateOf(initialProduct?.sizes?.joinToString(", ") ?: "4.25 Meters (Standard)") }
    var colorsStr by remember { mutableStateOf(initialProduct?.colors?.joinToString(", ") ?: "White, Cream, Black") }
    var isFeatured by remember { mutableStateOf(initialProduct?.isFeatured ?: false) }
    var isNewArrival by remember { mutableStateOf(initialProduct?.isNewArrival ?: true) }
    var isAvailable by remember { mutableStateOf(initialProduct?.isAvailable ?: true) }

    // Image upload and gallery state: preserve both existing images list and single imageUrl
    var images by remember {
        mutableStateOf(
            buildList {
                initialProduct?.imageUrl?.let { if (it.isNotBlank()) add(it) }
                initialProduct?.images?.forEach { if (it.isNotBlank() && !contains(it)) add(it) }
            }
        )
    }
    var customImageUrl by remember { mutableStateOf("") }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadStatusText by remember { mutableStateOf("") }
    var showPresetPicker by remember { mutableStateOf(false) }

    // Single photo picker (for main image or single addition)
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploadingImage = true
            uploadStatusText = "Uploading main image..."
            coroutineScope.launch {
                val result = ImageStorageHelper.uploadImageToFirebaseStorage(context, uri, "products")
                isUploadingImage = false
                uploadStatusText = ""
                if (result.isSuccess) {
                    val downloadUrl = result.getOrThrow()
                    // Set as first (main) image
                    images = listOf(downloadUrl) + images.filter { it != downloadUrl }
                    Toast.makeText(context, "Main image updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Upload failed"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Multiple photos picker (for batch additional image gallery uploads)
    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris ->
        if (uris.isNotEmpty()) {
            isUploadingImage = true
            coroutineScope.launch {
                var count = 0
                for ((idx, u) in uris.withIndex()) {
                    uploadStatusText = "Uploading image ${idx + 1} of ${uris.size}..."
                    val res = ImageStorageHelper.uploadImageToFirebaseStorage(context, u, "products")
                    if (res.isSuccess) {
                        val url = res.getOrThrow()
                        images = images + url
                        count++
                    }
                }
                isUploadingImage = false
                uploadStatusText = ""
                if (count > 0) {
                    Toast.makeText(context, "Added $count additional images to gallery!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialProduct == null) "Add New Product" else "Edit Product", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Availability Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isAvailable) MintEmeraldContainer.copy(alpha = 0.5f) else Color(0xFFFFECEC)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Product Availability",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isAvailable) DeepEmerald else Color(0xFFB91C1C)
                            )
                            Text(
                                text = if (isAvailable) "Available for Sale in Customer App" else "Out of Stock / Unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAvailable) EmeraldGreen else Color(0xFFDC2626)
                            )
                        }
                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { isAvailable = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldGreen,
                                checkedTrackColor = MintEmeraldContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PRODUCT IMAGES MANAGEMENT (Main Image + Multiple Additional Images)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Product Images (${images.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepEmerald
                        )
                        Text(
                            text = "First image = Main Cover • Others = Vertical Gallery",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isUploadingImage) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = EmeraldGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(uploadStatusText.ifBlank { "Uploading..." }, fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Images List with Reordering & Badges
                if (images.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        images.forEachIndexed { index, imgUrl ->
                            val isMainCover = index == 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMainCover) MintEmeraldContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                                ),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Image Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                                    ) {
                                        HameedProductImage(
                                            imageUrl = imgUrl,
                                            contentDescription = "Image $index",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Role label & index
                                    Column(modifier = Modifier.weight(1f)) {
                                        if (isMainCover) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = RegalGold, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "MAIN COVER IMAGE",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = DeepEmerald
                                                )
                                            }
                                            Text(
                                                text = "Displayed on home & catalog cards",
                                                fontSize = 10.sp,
                                                color = EmeraldGreen
                                            )
                                        } else {
                                            Text(
                                                text = "Gallery Photo #$index",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Shown in Temu-style vertical gallery",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Action buttons: Reorder, Set as Main, Remove
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Move Up / Left
                                        if (index > 0) {
                                            IconButton(
                                                onClick = {
                                                    val mutable = images.toMutableList()
                                                    val temp = mutable[index]
                                                    mutable[index] = mutable[index - 1]
                                                    mutable[index - 1] = temp
                                                    images = mutable
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Move earlier",
                                                    tint = DeepEmerald,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        // Move Down / Right
                                        if (index < images.size - 1) {
                                            IconButton(
                                                onClick = {
                                                    val mutable = images.toMutableList()
                                                    val temp = mutable[index]
                                                    mutable[index] = mutable[index + 1]
                                                    mutable[index + 1] = temp
                                                    images = mutable
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = "Move later",
                                                    tint = DeepEmerald,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        // Set as Main button
                                        if (!isMainCover) {
                                            IconButton(
                                                onClick = {
                                                    val mutable = images.toMutableList()
                                                    val target = mutable.removeAt(index)
                                                    mutable.add(0, target)
                                                    images = mutable
                                                    Toast.makeText(context, "Set as main cover image", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Set as Main",
                                                    tint = RegalGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        // Remove button
                                        IconButton(
                                            onClick = {
                                                images = images.toMutableList().apply { removeAt(index) }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete image",
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Image Upload Action Buttons: Pick Main Image vs Pick Multiple Additional Images
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Upload/Change Main Image
                    Button(
                        onClick = {
                            singlePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isUploadingImage,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (images.isEmpty()) EmeraldGreen else MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (images.isEmpty()) "Upload Main Cover" else "Change Main", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Add Multiple Additional Gallery Images
                    OutlinedButton(
                        onClick = {
                            multiPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isUploadingImage
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp), tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Gallery Photos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Presets button
                OutlinedButton(
                    onClick = { showPresetPicker = !showPresetPicker },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp), tint = RegalGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showPresetPicker) "Hide Preset Fabrics" else "Browse Preset Pakistani Fabrics", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepEmerald)
                }
                if (showPresetPicker) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Select Pakistani Fabric Texture:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            ImageStorageHelper.presetApparelImages.forEach { preset ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!images.contains(preset.url)) {
                                                images = images + preset.url
                                            }
                                            showPresetPicker = false
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(preset.name, fontSize = 12.sp, color = DeepEmerald)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(preset.category, fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Add Image URL Input
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customImageUrl,
                        onValueChange = { customImageUrl = it },
                        label = { Text("Paste Image URL (CDN / Web)", fontSize = 11.sp) },
                        placeholder = { Text("https://...", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            if (customImageUrl.isNotBlank()) {
                                images = images + customImageUrl.trim()
                                customImageUrl = ""
                            }
                        },
                        enabled = customImageUrl.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add image URL", tint = EmeraldGreen)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title / Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Category *", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                if (categories.isEmpty()) {
                    Text("No categories yet. Please add a category first.", color = Color(0xFFDC2626), fontSize = 11.sp)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { c ->
                            val isSelected = (categoryId.isNotBlank() && categoryId == c.id) ||
                                             category.equals(c.name, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    categoryId = c.id
                                    category = c.name
                                },
                                label = { Text(c.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Regular Price (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = salePriceStr,
                        onValueChange = { salePriceStr = it },
                        label = { Text("Sale Price (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stock Quantity (Units) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sizesStr,
                    onValueChange = { sizesStr = it },
                    label = { Text("Sizes (comma separated)") },
                    placeholder = { Text("e.g. Small, Medium, Large, 4.25 Meters") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = colorsStr,
                    onValueChange = { colorsStr = it },
                    label = { Text("Colors (comma separated)") },
                    placeholder = { Text("e.g. Pure White, Natural Cream, Navy") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product & Fabric Description *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Text("Featured on Home Screen", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isNewArrival, onCheckedChange = { isNewArrival = it })
                    Text("Mark as New Arrival", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isUploadingImage) {
                        Toast.makeText(context, "Please wait for image upload to finish before saving", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.length < 2) {
                        Toast.makeText(context, "Product title must be at least 2 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val chosenCategory = if (category.isNotBlank()) category else categories.find { it.id == categoryId }?.name.orEmpty()
                    if (chosenCategory.isBlank()) {
                        Toast.makeText(context, "Please select a valid category for this product", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val price = priceStr.toLongOrNull() ?: 0L
                    if (price <= 0) {
                        Toast.makeText(context, "Please enter a valid regular price in PKR", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val salePrice = salePriceStr.toLongOrNull() ?: 0L
                    val stock = stockStr.toLongOrNull() ?: 0L
                    if (stock < 0) {
                        Toast.makeText(context, "Stock quantity cannot be negative", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val sizes = sizesStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val colors = colorsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val finalImages = images.filter { it.isNotBlank() }
                    val finalImageUrl = finalImages.firstOrNull().orEmpty()

                    val product = (initialProduct ?: Product()).copy(
                        title = trimmedTitle,
                        description = description.trim(),
                        categoryId = categoryId,
                        category = chosenCategory,
                        price = price,
                        salePrice = salePrice,
                        stock = stock,
                        images = finalImages,
                        imageUrl = finalImageUrl,
                        sizes = sizes,
                        colors = colors,
                        isFeatured = isFeatured,
                        isNewArrival = isNewArrival,
                        isAvailable = isAvailable
                    )
                    onSave(product)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AdminSettingsView(
    settings: StoreSettings,
    onUpdateSettings: suspend (StoreSettings) -> Result<Unit>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var storeName by remember { mutableStateOf(settings.storeName) }
    var deliveryFeeStr by remember { mutableStateOf(settings.deliveryFee.toString()) }
    var freeThresholdStr by remember { mutableStateOf(settings.freeShippingThreshold.toString()) }
    var phone by remember { mutableStateOf(settings.phone) }
    var whatsapp by remember { mutableStateOf(settings.whatsapp) }
    var announcement by remember { mutableStateOf(settings.announcement) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder))),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Store Configuration & Nationwide Delivery",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryFeeStr,
                        onValueChange = { deliveryFeeStr = it },
                        label = { Text("Standard Delivery Fee (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = freeThresholdStr,
                        onValueChange = { freeThresholdStr = it },
                        label = { Text("Free Delivery Above (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Store Contact Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("Store WhatsApp Number (with country code e.g. +92)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = announcement,
                    onValueChange = { announcement = it },
                    label = { Text("Promotional Top Announcement Banner") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isSaving = true
                        coroutineScope.launch {
                            val fee = deliveryFeeStr.toLongOrNull() ?: 200L
                            val threshold = freeThresholdStr.toLongOrNull() ?: 5000L
                            val updated = settings.copy(
                                storeName = storeName.trim(),
                                deliveryFee = fee,
                                freeShippingThreshold = threshold,
                                phone = phone.trim(),
                                whatsapp = whatsapp.trim(),
                                announcement = announcement.trim()
                            )
                            val res = onUpdateSettings(updated)
                            isSaving = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "Settings updated successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to update settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    Text("Save Store Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminOverviewView(
    orders: List<Order>,
    products: List<Product>
) {
    val totalRevenue = orders
        .filter { it.status != "Cancelled" && it.status != "Returned" }
        .sumOf { it.totalAmount }

    val pendingCount = orders.count { it.status == "Pending" }
    val dispatchedCount = orders.count { it.status == "Dispatched" }
    val deliveredCount = orders.count { it.status == "Delivered" }
    val lowStockProducts = products.filter { it.stock <= 5 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sales Revenue Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepEmerald),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Total Gross Sales",
                    style = MaterialTheme.typography.labelSmall,
                    color = RichGoldAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatPkr(totalRevenue),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Across ${orders.size} customer Cash on Delivery orders",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Metrics 2x2 grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Pending Action",
                value = "$pendingCount",
                color = RegalGold,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Dispatched",
                value = "$dispatchedCount",
                color = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Delivered",
                value = "$deliveredCount",
                color = Color(0xFF16A34A),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Live Catalog",
                value = "${products.size} items",
                color = DeepEmerald,
                modifier = Modifier.weight(1f)
            )
        }

        // Low Stock Warnings
        if (lowStockProducts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFFCA5A5), Color(0xFFFCA5A5)))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Low Stock Alerts (${lowStockProducts.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFDC2626)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    lowStockProducts.forEach { p ->
                        Text(
                            text = "• ${p.title} (${p.category}): only ${p.stock} left in stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder))),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}
