package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Product(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val categoryId: String = "",
    val category: String = "",
    val price: Long = 0L,
    val salePrice: Long = 0L,
    val stock: Long = 0L,
    val images: List<String> = emptyList(),
    val imageUrl: String = "",
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val isNewArrival: Boolean = false,
    val isArchived: Boolean = false,
    val isActive: Boolean = true,
    val isAvailable: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectiveImageUrl: String
        get() = when {
            imageUrl.trim().isNotBlank() -> imageUrl.trim()
            images.isNotEmpty() && images.first().trim().isNotBlank() -> images.first().trim()
            else -> ""
        }

    val displayPrice: Long
        get() = if (salePrice > 0 && salePrice < price) salePrice else price

    val hasDiscount: Boolean
        get() = salePrice > 0 && salePrice < price

    val discountPercent: Int
        get() = if (hasDiscount && price > 0) {
            (((price - salePrice).toDouble() / price) * 100).toInt()
        } else 0
}
