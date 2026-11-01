package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class OrderItem(
    val productId: String = "",
    val title: String = "",
    val price: Long = 0L,
    val quantity: Long = 1L,
    val selectedSize: String = "",
    val selectedColor: String = "",
    val imageUrl: String = ""
)

@IgnoreExtraProperties
data class Order(
    val id: String = "",
    val orderNumber: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val city: String = "",
    val area: String = "",
    val deliveryAddress: String = "",
    val deliveryNotes: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Long = 0L,
    val deliveryFee: Long = 0L,
    val totalAmount: Long = 0L,
    val paymentMethod: String = "COD",
    val paymentStatus: String = "Unpaid", // Unpaid, Paid, Refunded
    val isGuest: Boolean = false,
    val status: String = "Pending", // Pending, Confirmed, Packed, Dispatched, Delivered, Cancelled, Returned
    val courierName: String = "",
    val trackingNumber: String = "",
    val commissionRate: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
