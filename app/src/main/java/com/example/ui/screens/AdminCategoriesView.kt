package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.HameedProductImage
import com.example.data.ImageStorageHelper
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintEmeraldContainer
import com.example.ui.theme.RegalGold
import com.example.ui.theme.RichGoldAccent
import kotlinx.coroutines.launch

@Composable
fun AdminCategoriesView(
    categories: List<Category>,
    products: List<Product>,
    onAddCategory: suspend (name: String, imageUrl: String, isActive: Boolean) -> Result<String>,
    onUpdateCategory: suspend (id: String, name: String, imageUrl: String, isActive: Boolean) -> Result<Unit>,
    onDeleteCategory: suspend (id: String) -> Result<Unit>,
    onSeedCategories: suspend () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isAddingCategory by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val filteredCategories = categories.filter { cat ->
        searchQuery.isBlank() || cat.name.contains(searchQuery, ignoreCase = true) || cat.id.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar with Add & Seed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isAddingCategory = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Category", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        onSeedCategories()
                        Toast.makeText(context, "Categories synchronized", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sync Defaults", fontSize = 12.sp)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search categories...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = EmeraldGreen) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = CardBorder
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Categories List
        if (filteredCategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = DeepEmerald.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No categories matching \"$searchQuery\"" else "No categories found in store.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCategories, key = { it.id }) { cat ->
                    val assignedProductsCount = products.count { p ->
                        p.categoryId == cat.id || p.category.equals(cat.name, ignoreCase = true)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category image thumbnail or icon
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                HameedProductImage(
                                    imageUrl = cat.imageUrl,
                                    contentDescription = cat.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    fallbackCategory = cat.name
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DeepEmerald
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (cat.isActive) MintEmeraldContainer else Color(0xFFFFECEC),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (cat.isActive) "ACTIVE" else "INACTIVE",
                                            color = if (cat.isActive) EmeraldGreen else Color(0xFFDC2626),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ID: ${cat.id}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$assignedProductsCount product(s) assigned",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (assignedProductsCount > 0) EmeraldGreen else Color.Gray,
                                    fontWeight = if (assignedProductsCount > 0) FontWeight.Medium else FontWeight.Normal
                                )
                            }

                            // Active Switch
                            Switch(
                                checked = cat.isActive,
                                onCheckedChange = { newStatus ->
                                    coroutineScope.launch {
                                        val res = onUpdateCategory(cat.id, cat.name, cat.imageUrl, newStatus)
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Category ${if (newStatus) "activated" else "deactivated"}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldGreen,
                                    checkedTrackColor = MintEmeraldContainer
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            // Edit Button
                            IconButton(onClick = { categoryToEdit = cat }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Category", tint = DeepEmerald)
                            }

                            // Delete Button
                            IconButton(onClick = { categoryToDelete = cat }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Category Dialog
    if (isAddingCategory) {
        AdminCategoryDialog(
            category = null,
            onDismiss = { isAddingCategory = false },
            onSave = { name, imageUrl, isActive ->
                coroutineScope.launch {
                    val res = onAddCategory(name, imageUrl, isActive)
                    if (res.isSuccess) {
                        Toast.makeText(context, "Category '$name' created successfully", Toast.LENGTH_SHORT).show()
                        isAddingCategory = false
                    } else {
                        Toast.makeText(context, "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Edit Category Dialog
    if (categoryToEdit != null) {
        AdminCategoryDialog(
            category = categoryToEdit,
            onDismiss = { categoryToEdit = null },
            onSave = { name, imageUrl, isActive ->
                coroutineScope.launch {
                    val res = onUpdateCategory(categoryToEdit!!.id, name, imageUrl, isActive)
                    if (res.isSuccess) {
                        Toast.makeText(context, "Category '$name' updated", Toast.LENGTH_SHORT).show()
                        categoryToEdit = null
                    } else {
                        Toast.makeText(context, "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Delete Category Confirmation Dialog
    if (categoryToDelete != null) {
        val targetCat = categoryToDelete!!
        val assignedCount = products.count { p ->
            p.categoryId == targetCat.id || p.category.equals(targetCat.name, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            icon = {
                Icon(
                    imageVector = if (assignedCount > 0) Icons.Default.Warning else Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (assignedCount > 0) "Cannot Delete Category" else "Delete Category?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (assignedCount > 0) {
                    Text(
                        text = "The category \"${targetCat.name}\" currently has $assignedCount product(s) assigned to it.\n\nPlease reassign or delete these products before deleting this category.",
                        color = Color(0xFF991B1B)
                    )
                } else {
                    Text("Are you sure you want to permanently delete the category \"${targetCat.name}\"? This action cannot be undone.")
                }
            },
            confirmButton = {
                if (assignedCount == 0) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val res = onDeleteCategory(targetCat.id)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
                                    categoryToDelete = null
                                } else {
                                    Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToDelete = null }) {
                    Text(if (assignedCount > 0) "Got it" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminCategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, imageUrl: String, isActive: Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(category?.name ?: "") }
    var imageUrl by remember { mutableStateOf(category?.imageUrl ?: "") }
    var isActive by remember { mutableStateOf(category?.isActive ?: true) }
    var isUploadingImage by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploadingImage = true
            coroutineScope.launch {
                val result = ImageStorageHelper.uploadImageToFirebaseStorage(context, uri, "categories")
                isUploadingImage = false
                if (result.isSuccess) {
                    imageUrl = result.getOrThrow()
                    Toast.makeText(context, "Category image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "Upload failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (category == null) "Add New Category" else "Edit Category",
                fontWeight = FontWeight.Bold,
                color = DeepEmerald
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name *") },
                    placeholder = { Text("e.g. Peshawari Chappal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Image Preview
                Text("Category Image", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                if (imageUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    ) {
                        HameedProductImage(
                            imageUrl = imageUrl,
                            contentDescription = "Category image preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { imageUrl = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Pick from Gallery (Firebase Storage)
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isUploadingImage
                ) {
                    if (isUploadingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uploading to Firebase Storage...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Category Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Image URL manual input
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Or Paste Image Download URL", fontSize = 11.sp) },
                    placeholder = { Text("https://...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Category Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (isActive) "Visible to customers" else "Hidden from customer catalog",
                            fontSize = 11.sp,
                            color = if (isActive) EmeraldGreen else Color.Gray
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGreen,
                            checkedTrackColor = MintEmeraldContainer
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isUploadingImage) {
                        Toast.makeText(context, "Please wait for category image upload to finish", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val trimmed = name.trim()
                    if (trimmed.length < 2) {
                        Toast.makeText(context, "Category name must be at least 2 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onSave(trimmed, imageUrl.trim(), isActive)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color.White
                )
            ) {
                Text(if (category == null) "Create Category" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
