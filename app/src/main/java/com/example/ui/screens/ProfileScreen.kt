package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.StoreSettings
import com.example.data.model.UserProfile
import com.example.data.session.GuestSessionManager
import com.example.ui.auth.onGoogleSignInClicked
import com.example.ui.auth.signOutUser
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintEmeraldContainer
import com.example.ui.theme.RegalGold
import com.example.ui.theme.RichGoldAccent
import com.example.ui.theme.WarmGoldContainer
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    settings: StoreSettings,
    ordersCount: Int,
    wishlistCount: Int,
    onSaveProfile: suspend (UserProfile) -> Result<Unit>,
    onNavigateToAdmin: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onSignInSuccess: () -> Unit,
    onSignOutComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val currentUser = Firebase.auth.currentUser

    // Profile form state
    var name by remember(userProfile, currentUser) {
        mutableStateOf(userProfile?.name ?: currentUser?.displayName ?: GuestSessionManager.guestName)
    }
    var phone by remember(userProfile) {
        mutableStateOf(userProfile?.phone ?: GuestSessionManager.guestPhone)
    }
    var city by remember(userProfile) {
        mutableStateOf(userProfile?.city.takeIf { !it.isNullOrBlank() } ?: GuestSessionManager.guestCity)
    }
    var area by remember(userProfile) {
        mutableStateOf(userProfile?.area ?: GuestSessionManager.guestArea)
    }
    var postalCode by remember(userProfile) {
        mutableStateOf(userProfile?.postalCode ?: "")
    }
    var address by remember(userProfile) {
        mutableStateOf(userProfile?.address ?: GuestSessionManager.guestAddress)
    }

    var isSaving by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    val isAdmin = currentUser?.email == "hameedullahkhattak370@gmail.com" ||
                  userProfile?.role == "admin"

    val popularPakistaniCities = listOf(
        "Lahore", "Karachi", "Islamabad", "Rawalpindi", "Peshawar",
        "Faisalabad", "Multan", "Quetta", "Sialkot", "Gujranwala"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("customer_profile_screen")
    ) {
        if (currentUser != null) {
            // ================= REGISTERED CUSTOMER HEADER =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(DeepEmerald, EmeraldGreen)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentUser.photoUrl != null) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, RichGoldAccent, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MintEmeraldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = if (name.isNotBlank()) name else (currentUser.displayName ?: "Valued Customer"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentUser.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = if (isAdmin) RichGoldAccent else MintEmeraldContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isAdmin) "STORE ADMIN" else "REGISTERED CUSTOMER",
                                    color = if (isAdmin) DeepEmerald else EmeraldGreen,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToOrders() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder))),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$ordersCount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        )
                        Text(
                            text = "My Orders (View)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder))),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$wishlistCount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = RegalGold
                            )
                        )
                        Text(
                            text = "Saved in Wishlist",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // ================= GUEST VISITOR HEADER & SIGN-IN OPTION =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(DeepEmerald, EmeraldGreen)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_hameed),
                            contentDescription = "Hameed Logo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.dp, RichGoldAccent, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Guest Shopper",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Welcome to Hameed Collection",
                                style = MaterialTheme.typography.bodySmall,
                                color = RichGoldAccent
                            )
                            Text(
                                text = "Shop freely without mandatory sign-in",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sign In / Create Account Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = WarmGoldContainer),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(RichGoldAccent, RegalGold))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = DeepEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sign In or Create Account",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepEmerald
                            )
                            Text(
                                text = "Save your delivery address & access order history on any device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isSigningIn = true
                            authError = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isSigningIn = false
                                    onSignInSuccess()
                                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onAuthError = { err ->
                                    isSigningIn = false
                                    authError = err
                                },
                                onAuthCancelled = {
                                    isSigningIn = false
                                },
                                scope = coroutineScope
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepEmerald,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("google_signin_button"),
                        enabled = !isSigningIn
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                color = RichGoldAccent,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = RichGoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In with Google", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Admin Portal Button (If user is store owner)
        if (isAdmin) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAdmin() },
                colors = CardDefaults.cardColors(containerColor = WarmGoldContainer),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(RichGoldAccent, RegalGold))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = RichGoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Store Admin Portal",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepEmerald
                        )
                        Text(
                            text = "Manage products, inventory, customer & guest orders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Admin",
                        tint = DeepEmerald
                    )
                }
            }
        }

        // Orders shortcut for registered customers
        if (currentUser != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToOrders() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Order History & Live Tracking",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = DeepEmerald
                        )
                        Text(
                            text = "Track your dispatched parcels & delivery updates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = EmeraldGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile / Delivery Preferences Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder))),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentUser != null) "Saved Delivery Profile" else "Guest Session Delivery Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepEmerald
                    )
                }
                Text(
                    text = if (currentUser != null) {
                        "Securely stored in Firebase. Auto-fills on any device when you sign in."
                    } else {
                        "Pre-fills your delivery details during this session for quick checkout."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mobile / Contact Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Active Contact / Mobile Number *") },
                    placeholder = { Text("03001234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // City
                Text(
                    text = "City: $city",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(popularPakistaniCities) { c ->
                        FilterChip(
                            selected = city.equals(c, ignoreCase = true),
                            onClick = { city = c },
                            label = { Text(c, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Area / Locality
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Locality / Sector *") },
                    placeholder = { Text("e.g. Gulberg III, DHA Phase 5, F-10, Hayatabad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Postal Code (optional)
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Postal Code (Optional)") },
                    placeholder = { Text("e.g. 54000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Street Delivery Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Complete Street Delivery Address *") },
                    placeholder = { Text("House #, Street #, Mohalla / Block, Nearest Landmark") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (currentUser != null) {
                            isSaving = true
                            coroutineScope.launch {
                                val updated = (userProfile ?: UserProfile(
                                    userId = currentUser.uid,
                                    email = currentUser.email ?: ""
                                )).copy(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    city = city.trim(),
                                    area = area.trim(),
                                    postalCode = postalCode.trim(),
                                    address = address.trim()
                                )
                                val res = onSaveProfile(updated)
                                isSaving = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Profile saved to Firebase!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to save profile: ${res.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            // Update guest session
                            GuestSessionManager.updateGuestDetails(
                                name = name.trim(),
                                phone = phone.trim(),
                                city = city.trim(),
                                area = area.trim(),
                                address = address.trim()
                            )
                            Toast.makeText(context, "Guest session details updated for checkout!", Toast.LENGTH_SHORT).show()
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
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = RichGoldAccent,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentUser != null) "Save Profile to Firebase" else "Update Session Details",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (currentUser != null) {
            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            OutlinedButton(
                onClick = {
                    signOutUser(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = onSignOutComplete,
                        scope = coroutineScope
                    )
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Account", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
