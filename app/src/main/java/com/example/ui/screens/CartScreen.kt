package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.HameedProductImage
import com.example.data.cart.CartItem
import com.example.data.cart.CartManager
import com.example.data.cart.formatPkr
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.StoreSettings
import com.example.data.model.UserProfile
import com.example.data.session.GuestSessionManager
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintEmeraldContainer
import com.example.ui.theme.RegalGold
import com.example.ui.theme.RichGoldAccent
import com.example.ui.theme.WarmGoldContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    userProfile: UserProfile?,
    settings: StoreSettings,
    onPlaceOrder: suspend (Order) -> Result<String>,
    onUpdateProfile: (suspend (UserProfile) -> Result<Unit>)? = null,
    onNavigateToCatalog: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val cartItems by CartManager.items.collectAsState()
    val subtotal = cartItems.sumOf { it.totalPrice }
    val freeShipping = subtotal >= settings.freeShippingThreshold
    val deliveryFee = if (freeShipping || subtotal == 0L) 0L else settings.deliveryFee
    val totalAmount = subtotal + deliveryFee

    var isCheckoutSheetVisible by remember { mutableStateOf(false) }
    var isConfirmationDialogOpen by remember { mutableStateOf(false) }
    var placedOrderNumber by remember { mutableStateOf<String?>(null) }
    var isPlacingOrder by remember { mutableStateOf(false) }
    var orderError by remember { mutableStateOf<String?>(null) }

    // Checkout Form state pre-populated with customer profile (if registered) or guest session
    var customerName by remember(userProfile) {
        mutableStateOf(userProfile?.name.takeIf { !it.isNullOrBlank() } ?: GuestSessionManager.guestName)
    }
    var customerPhone by remember(userProfile) {
        mutableStateOf(userProfile?.phone.takeIf { !it.isNullOrBlank() } ?: GuestSessionManager.guestPhone)
    }
    var city by remember(userProfile) {
        mutableStateOf(userProfile?.city.takeIf { !it.isNullOrBlank() } ?: GuestSessionManager.guestCity)
    }
    var area by remember(userProfile) {
        mutableStateOf(userProfile?.area.takeIf { !it.isNullOrBlank() } ?: GuestSessionManager.guestArea)
    }
    var deliveryAddress by remember(userProfile) {
        mutableStateOf(userProfile?.address.takeIf { !it.isNullOrBlank() } ?: GuestSessionManager.guestAddress)
    }
    var deliveryNotes by remember {
        mutableStateOf(GuestSessionManager.guestNotes)
    }
    var shouldUpdateSavedProfile by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val popularPakistaniCities = listOf(
        "Karachi", "Lahore", "Islamabad", "Rawalpindi", "Peshawar",
        "Multan", "Faisalabad", "Quetta", "Gujranwala", "Sialkot",
        "Abbottabad", "Hyderabad", "Mardan", "Swat", "Bannu", "Kohat"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MintEmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Cart is Empty",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = DeepEmerald
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore our exquisite collection of Pakistani unstitched fabrics, kurtas, and waistcoats.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToCatalog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Explore Collections", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("cart_items_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Free Shipping Progress Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WarmGoldContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = EmeraldGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (freeShipping) {
                                    Text(
                                        text = "Congratulations! You have FREE nationwide delivery!",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldGreen
                                    )
                                } else {
                                    val needed = settings.freeShippingThreshold - subtotal
                                    Text(
                                        text = "Add ${formatPkr(needed)} more to get FREE Delivery!",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DeepEmerald
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val progress = (subtotal.toFloat() / settings.freeShippingThreshold.toFloat()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EmeraldGreen,
                                trackColor = CardBorder
                            )
                        }
                    }
                }

                // Cart Items
                items(cartItems) { item ->
                    CartItemCard(
                        item = item,
                        onQuantityChange = { newQty ->
                            CartManager.updateQuantity(
                                productId = item.product.id,
                                size = item.selectedSize,
                                color = item.selectedColor,
                                newQuantity = newQty
                            )
                        },
                        onRemove = {
                            CartManager.removeItem(
                                productId = item.product.id,
                                size = item.selectedSize,
                                color = item.selectedColor
                            )
                        }
                    )
                }

                // Bill Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder))),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Order Summary",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepEmerald
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatPkr(subtotal), fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Charges", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (deliveryFee == 0L) {
                                    Text("FREE", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(formatPkr(deliveryFee), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment Method", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Cash on Delivery (COD)", color = DeepEmerald, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = CardBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total Payable",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    formatPkr(totalAmount),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Checkout Bottom Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total (COD)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatPkr(totalAmount),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        )
                    }

                    Button(
                        onClick = { isCheckoutSheetVisible = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("proceed_to_checkout_button")
                    ) {
                        Text("Proceed to Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Delivery Address & COD Placement
    if (isCheckoutSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isCheckoutSheetVisible = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Delivery Details (Cash on Delivery)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Please provide your Pakistani contact and address details for courier dispatch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Shopper Status Banner
                Surface(
                    color = if (userProfile != null) MintEmeraldContainer else WarmGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (userProfile != null) EmeraldGreen else RegalGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (userProfile != null) {
                                "Saved Profile: ${userProfile.name.ifBlank { "Registered Customer" }} (Details prefilled)"
                            } else {
                                "Guest Checkout: No account or sign-in needed to place your order"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = DeepEmerald
                        )
                    }
                }

                if (orderError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = orderError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Recipient Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pakistani Phone Number
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Active Mobile / Contact Number *") },
                    placeholder = { Text("03001234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_phone_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // City Chips Selection
                Text(
                    text = "Destination City: $city",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(popularPakistaniCities) { c ->
                        val isSelected = city.equals(c, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { city = c },
                            label = { Text(c, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Area / Locality Input
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Locality / Sector *") },
                    placeholder = { Text("e.g. DHA Phase 5, Gulberg III, Hayatabad, F-10") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_area_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Complete Delivery Address
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Complete Street Delivery Address *") },
                    placeholder = { Text("House/Flat #, Street #, Mohalla/Block, Nearest Landmark") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_address_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Delivery Notes
                OutlinedTextField(
                    value = deliveryNotes,
                    onValueChange = { deliveryNotes = it },
                    label = { Text("Special Delivery Instructions (Optional)") },
                    placeholder = { Text("e.g. Call before delivery, deliver in afternoon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                // Option to update saved profile for registered users
                if (userProfile != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { shouldUpdateSavedProfile = !shouldUpdateSavedProfile },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = shouldUpdateSavedProfile,
                            onCheckedChange = { shouldUpdateSavedProfile = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save this delivery address to my permanent profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // COD notice card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarmGoldContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Payment: Cash on Delivery (COD)",
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Pay ${formatPkr(totalAmount)} to the courier rider upon delivery. No upfront payment required.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Proceed to Confirmation Button
                Button(
                    onClick = {
                        if (customerName.trim().length < 2) {
                            orderError = "Please enter your recipient full name"
                            return@Button
                        }
                        val cleanPhone = customerPhone.replace(Regex("[^0-9+]"), "")
                        if (cleanPhone.length < 10) {
                            orderError = "Please enter an active contact number (e.g. 03001234567)"
                            return@Button
                        }
                        if (city.isBlank()) {
                            orderError = "Please select or enter destination city"
                            return@Button
                        }
                        if (area.trim().length < 2) {
                            orderError = "Please enter your area or locality"
                            return@Button
                        }
                        if (deliveryAddress.trim().length < 5) {
                            orderError = "Please enter complete street delivery address"
                            return@Button
                        }

                        orderError = null
                        isConfirmationDialogOpen = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("proceed_to_order_confirmation_button"),
                    enabled = !isPlacingOrder
                ) {
                    Text(
                        text = "Review & Place COD Order (${formatPkr(totalAmount)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Customer Order Confirmation Dialog
    if (isConfirmationDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                if (!isPlacingOrder) isConfirmationDialogOpen = false
            },
            title = {
                Text(
                    text = "Confirm Your COD Order",
                    color = DeepEmerald,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Please verify your delivery details before placing the order:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Recipient: ${customerName.trim()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Phone: ${customerPhone.trim()}", fontSize = 12.sp)
                            Text("City & Area: ${city.trim()}, ${area.trim()}", fontSize = 12.sp)
                            Text("Address: ${deliveryAddress.trim()}", fontSize = 12.sp)
                            if (deliveryNotes.isNotBlank()) {
                                Text("Instructions: ${deliveryNotes.trim()}", fontSize = 11.sp, color = RegalGold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Items summary
                    Text("Items Summary (${cartItems.sumOf { it.quantity }} items):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity}x ${item.product.title}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(formatPkr(item.totalPrice), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 12.sp)
                        Text(formatPkr(subtotal), fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Charges", fontSize = 12.sp)
                        Text(if (deliveryFee == 0L) "FREE" else formatPkr(deliveryFee), fontSize = 12.sp, color = if (deliveryFee == 0L) EmeraldGreen else MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payable on Delivery", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepEmerald)
                        Text(formatPkr(totalAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepEmerald)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isPlacingOrder) return@Button
                        isPlacingOrder = true
                        orderError = null

                        coroutineScope.launch {
                            val orderItems = cartItems.map { ci ->
                                OrderItem(
                                    productId = ci.product.id,
                                    title = ci.product.title,
                                    price = ci.unitPrice,
                                    quantity = ci.quantity.toLong(),
                                    selectedSize = ci.selectedSize,
                                    selectedColor = ci.selectedColor,
                                    imageUrl = ci.product.images.firstOrNull() ?: ""
                                )
                            }

                            val newOrder = Order(
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                city = city.trim(),
                                area = area.trim(),
                                deliveryAddress = deliveryAddress.trim(),
                                deliveryNotes = deliveryNotes.trim(),
                                items = orderItems,
                                subtotal = subtotal,
                                deliveryFee = deliveryFee,
                                totalAmount = totalAmount,
                                paymentMethod = "COD",
                                paymentStatus = "Unpaid",
                                isGuest = (userProfile == null),
                                status = "Pending"
                            )

                            val result = onPlaceOrder(newOrder)
                            isPlacingOrder = false
                            if (result.isSuccess) {
                                placedOrderNumber = result.getOrThrow()

                                // Retain guest info in session
                                if (userProfile == null) {
                                    GuestSessionManager.updateGuestDetails(
                                        name = customerName.trim(),
                                        phone = customerPhone.trim(),
                                        city = city.trim(),
                                        area = area.trim(),
                                        address = deliveryAddress.trim(),
                                        notes = deliveryNotes.trim()
                                    )
                                } else if (shouldUpdateSavedProfile) {
                                    onUpdateProfile?.invoke(
                                        userProfile.copy(
                                            name = customerName.trim(),
                                            phone = customerPhone.trim(),
                                            city = city.trim(),
                                            area = area.trim(),
                                            address = deliveryAddress.trim()
                                        )
                                    )
                                }

                                CartManager.clearCart()
                                isConfirmationDialogOpen = false
                                isCheckoutSheetVisible = false
                            } else {
                                orderError = result.exceptionOrNull()?.localizedMessage ?: "Failed to place order"
                                isConfirmationDialogOpen = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                    enabled = !isPlacingOrder,
                    modifier = Modifier.testTag("confirm_cod_order_button")
                ) {
                    if (isPlacingOrder) {
                        CircularProgressIndicator(color = RichGoldAccent, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Confirm & Submit Order")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { isConfirmationDialogOpen = false },
                    enabled = !isPlacingOrder
                ) {
                    Text("Edit Details")
                }
            }
        )
    }

    // Order Success Dialog
    if (placedOrderNumber != null) {
        AlertDialog(
            onDismissRequest = { placedOrderNumber = null },
            confirmButton = {
                Button(
                    onClick = {
                        placedOrderNumber = null
                        onNavigateToOrders()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("View My Orders")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        placedOrderNumber = null
                        onNavigateToCatalog()
                    }
                ) {
                    Text("Continue Shopping")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order Placed Successfully!", color = DeepEmerald, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Thank you for shopping with Hameed Collection!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MintEmeraldContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Order Reference Number:",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGreen
                            )
                            Text(
                                text = placedOrderNumber ?: "",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepEmerald
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your order has been recorded. Our team will pack and dispatch it soon. You will pay Cash on Delivery when courier arrives (3-5 days).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Thumbnail
            val thumbUrl = item.product.effectiveImageUrl
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                HameedProductImage(
                    imageUrl = thumbUrl,
                    contentDescription = item.product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallbackCategory = item.product.category.ifBlank { "Hameed Collection" }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.selectedSize} | ${item.selectedColor}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatPkr(item.totalPrice),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )
            }

            // Quantity stepper & delete
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove item",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
