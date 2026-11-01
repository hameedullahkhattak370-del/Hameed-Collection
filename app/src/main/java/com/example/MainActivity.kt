package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.cart.CartManager
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.StoreSettingsRepository
import com.example.data.repository.UserRepository
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintEmeraldContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RegalGold
import com.example.ui.theme.RichGoldAccent
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    // Silent background auto sign-in attempt (without blocking visitor UI)
    LaunchedEffect(Unit) {
        val credentialManager = CredentialManager.create(context)
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onUnauthenticated = { /* Continue as guest without blocking */ },
            scope = coroutineScope
        )
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Direct launch into the store — Guest shopping is immediately available!
    MainShoppingApp(
        currentUser = currentUser,
        onSignInSuccess = { currentUser = Firebase.auth.currentUser },
        onSignOut = { currentUser = null }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShoppingApp(
    currentUser: FirebaseUser?,
    onSignInSuccess: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = currentUser?.uid ?: ""

    // Initialize repositories using named firestore database
    val productRepo = remember { ProductRepository(context) }
    val categoryRepo = remember { com.example.data.repository.CategoryRepository(context) }
    val orderRepo = remember { OrderRepository(context) }
    val userRepo = remember { UserRepository(context) }
    val settingsRepo = remember { StoreSettingsRepository(context) }

    // Observe data streams
    val products by productRepo.observeProducts().collectAsState(initial = emptyList())
    val allCategories by categoryRepo.observeCategories().collectAsState(initial = emptyList())
    val activeCategories by categoryRepo.observeActiveCategories().collectAsState(initial = emptyList())
    val settings by settingsRepo.observeSettings().collectAsState(initial = StoreSettings())
    val userProfile by userRepo.observeProfile(currentUserId).collectAsState(initial = null)
    val customerOrders by orderRepo.observeCustomerOrders(currentUserId).collectAsState(initial = emptyList())
    val cartItems by CartManager.items.collectAsState()

    val isAdmin = currentUser?.email == "hameedullahkhattak370@gmail.com" ||
                  userProfile?.role == "admin"

    // Seed initial Pakistani apparel catalogue and categories if database is fresh and current user is store admin
    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            categoryRepo.seedInitialCategoriesIfEmpty()
            productRepo.seedInitialCatalogIfEmpty()
        }
    }

    // If admin, observe all store orders for fulfillment management
    val adminOrders by if (isAdmin) {
        orderRepo.observeAllOrdersForAdmin().collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<Order>()) }
    }

    val wishlist = userProfile?.wishlist ?: emptyList()
    val wishlistProducts = products.filter { wishlist.contains(it.id) }

    // Navigation and screen state
    var selectedBottomNavIndex by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var productDetailHistory by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isAdminPortalOpen by remember { mutableStateOf(false) }
    var isViewingOrders by remember { mutableStateOf(false) }

    // Back handling for overlays
    if (isViewingOrders) {
        BackHandler { isViewingOrders = false }
    }
    if (selectedProductForDetail != null) {
        BackHandler {
            if (productDetailHistory.size > 1) {
                val remaining = productDetailHistory.dropLast(1)
                productDetailHistory = remaining
                selectedProductForDetail = remaining.last()
            } else {
                productDetailHistory = emptyList()
                selectedProductForDetail = null
            }
        }
    }
    if (isAdminPortalOpen) {
        BackHandler { isAdminPortalOpen = false }
    }

    // Detail Screen Overlay
    if (selectedProductForDetail != null) {
        ProductDetailScreen(
            product = selectedProductForDetail!!,
            allProducts = products,
            isFavorite = wishlist.contains(selectedProductForDetail!!.id),
            onBack = {
                if (productDetailHistory.size > 1) {
                    val remaining = productDetailHistory.dropLast(1)
                    productDetailHistory = remaining
                    selectedProductForDetail = remaining.last()
                } else {
                    productDetailHistory = emptyList()
                    selectedProductForDetail = null
                }
            },
            onProductClick = { newProduct ->
                productDetailHistory = productDetailHistory + newProduct
                selectedProductForDetail = newProduct
            },
            onFavoriteToggle = {
                coroutineScope.launch {
                    userRepo.toggleWishlist(selectedProductForDetail!!.id)
                }
            },
            onNavigateToCart = {
                productDetailHistory = emptyList()
                selectedProductForDetail = null
                isViewingOrders = false
                selectedBottomNavIndex = 2 // Switch to Cart tab
            }
        )
        return
    }

    // Admin Portal Screen Overlay
    if (isAdminPortalOpen) {
        AdminPortalScreen(
            orders = adminOrders,
            products = products,
            categories = allCategories,
            settings = settings,
            onBack = { isAdminPortalOpen = false },
            onAddProduct = { product -> productRepo.addProduct(product) },
            onUpdateProduct = { product -> productRepo.updateProduct(product) },
            onDeleteProduct = { id -> productRepo.deleteProduct(id) },
            onAddCategory = { name, imageUrl, isActive -> categoryRepo.addCategory(name, imageUrl, isActive) },
            onUpdateCategory = { id, name, imageUrl, isActive -> categoryRepo.updateCategory(id, name, imageUrl, isActive) },
            onDeleteCategory = { id ->
                val targetCategoryName = allCategories.find { it.id == id }?.name.orEmpty()
                val isAssigned = products.any { p ->
                    p.categoryId == id || (targetCategoryName.isNotBlank() && p.category.equals(targetCategoryName, ignoreCase = true))
                }
                categoryRepo.deleteCategory(id, isAssigned)
            },
            onUpdateOrderStatus = { id, status, courier, tracking ->
                orderRepo.updateOrderStatus(id, status, courier, tracking)
            },
            onUpdateStoreSettings = { s -> settingsRepo.updateSettings(s) },
            onSeedCatalog = { productRepo.seedInitialCatalogIfEmpty(force = true) },
            onSeedCategories = { categoryRepo.seedInitialCategoriesIfEmpty() }
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_hameed),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "HAMEED COLLECTION",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = DeepEmerald
                            )
                            Text(
                                text = "Exclusive Pakistani Fashion",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = RegalGold
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isViewingOrders) {
                        IconButton(onClick = { isViewingOrders = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = DeepEmerald
                            )
                        }
                    }
                },
                actions = {
                    // WhatsApp Store Support
                    IconButton(
                        onClick = {
                            val rawPhone = settings.whatsapp.ifBlank { "+923271825407" }
                            val cleanDigits = rawPhone.replace(Regex("[^0-9]"), "")
                            val targetPhone = if (cleanDigits.isNotBlank() && cleanDigits != "923001234567") cleanDigits else "923271825407"
                            val prefilledMsg = "Assalam-o-Alaikum! I would like to inquire about a product from Hameed Collection."
                            val url = "https://wa.me/$targetPhone?text=${Uri.encode(prefilledMsg)}"
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
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Contact WhatsApp Support",
                            tint = EmeraldGreen
                        )
                    }

                    // Search Shortcut
                    IconButton(
                        onClick = {
                            isViewingOrders = false
                            selectedCategoryFilter = null
                            selectedBottomNavIndex = 1 // Switch to Categories/Search
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Catalog",
                            tint = DeepEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Bottom navigation: Home, Categories, Cart and My Profile
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // 1. Home
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 0 && !isViewingOrders,
                    onClick = {
                        isViewingOrders = false
                        selectedBottomNavIndex = 0
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = MintEmeraldContainer
                    )
                )

                // 2. Categories
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 1 && !isViewingOrders,
                    onClick = {
                        isViewingOrders = false
                        selectedCategoryFilter = null
                        selectedBottomNavIndex = 1
                    },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Categories") },
                    label = { Text("Categories", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = MintEmeraldContainer
                    )
                )

                // 3. Cart (with badge count)
                val cartCount = cartItems.sumOf { it.quantity }
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 2 && !isViewingOrders,
                    onClick = {
                        isViewingOrders = false
                        selectedBottomNavIndex = 2
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = RichGoldAccent,
                                        contentColor = DeepEmerald
                                    ) {
                                        Text("$cartCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    },
                    label = { Text("Cart", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = MintEmeraldContainer
                    )
                )

                // 4. My Profile
                val pendingOrders = customerOrders.count { it.status == "Pending" || it.status == "Dispatched" }
                NavigationBarItem(
                    selected = (selectedBottomNavIndex == 3 || isViewingOrders),
                    onClick = {
                        isViewingOrders = false
                        selectedBottomNavIndex = 3
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingOrders > 0) {
                                    Badge(
                                        containerColor = EmeraldGreen,
                                        contentColor = Color.White
                                    ) {
                                        Text("$pendingOrders")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "My Profile")
                        }
                    },
                    label = { Text("My Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = MintEmeraldContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isViewingOrders) {
                OrdersScreen(
                    orders = customerOrders,
                    wishlistProducts = wishlistProducts,
                    settings = settings,
                    isLoading = false,
                    onCancelOrder = { id -> orderRepo.cancelCustomerOrder(id) },
                    onProductClick = {
                        productDetailHistory = listOf(it)
                        selectedProductForDetail = it
                    },
                    onRemoveFromWishlist = { product ->
                        coroutineScope.launch { userRepo.toggleWishlist(product.id) }
                    }
                )
            } else {
                when (selectedBottomNavIndex) {
                    0 -> HomeScreen(
                        products = products,
                        categories = activeCategories,
                        settings = settings,
                        isLoading = products.isEmpty(),
                        wishlist = wishlist,
                        onProductClick = {
                            productDetailHistory = listOf(it)
                            selectedProductForDetail = it
                        },
                        onFavoriteToggle = { product ->
                            coroutineScope.launch { userRepo.toggleWishlist(product.id) }
                        },
                        onNavigateToCatalog = { cat ->
                            selectedCategoryFilter = cat
                            selectedBottomNavIndex = 1
                        },
                        onNavigateToCart = { selectedBottomNavIndex = 2 }
                    )
                    1 -> CatalogScreen(
                        products = products,
                        categories = activeCategories,
                        isLoading = products.isEmpty(),
                        wishlist = wishlist,
                        initialCategory = selectedCategoryFilter,
                        onProductClick = {
                            productDetailHistory = listOf(it)
                            selectedProductForDetail = it
                        },
                        onFavoriteToggle = { product ->
                            coroutineScope.launch { userRepo.toggleWishlist(product.id) }
                        }
                    )
                    2 -> CartScreen(
                        userProfile = userProfile,
                        settings = settings,
                        onPlaceOrder = { order -> orderRepo.placeOrder(order) },
                        onUpdateProfile = { profile -> userRepo.saveProfile(profile) },
                        onNavigateToCatalog = { selectedBottomNavIndex = 1 },
                        onNavigateToOrders = { isViewingOrders = true }
                    )
                    3 -> ProfileScreen(
                        userProfile = userProfile,
                        settings = settings,
                        ordersCount = customerOrders.size,
                        wishlistCount = wishlist.size,
                        onSaveProfile = { profile -> userRepo.saveProfile(profile) },
                        onNavigateToAdmin = { isAdminPortalOpen = true },
                        onNavigateToOrders = { isViewingOrders = true },
                        onSignInSuccess = onSignInSuccess,
                        onSignOutComplete = onSignOut
                    )
                }
            }
        }
    }
}
