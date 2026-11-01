package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class StoreSettings(
    val id: String = "config",
    val storeName: String = "Hameed Collection",
    val phone: String = "+923271825407",
    val whatsapp: String = "+923271825407",
    val deliveryFee: Long = 200L,
    val freeShippingThreshold: Long = 5000L,
    val announcement: String = "Free Delivery on orders above PKR 5,000 across Pakistan | Cash on Delivery available",
    val bannerUrls: List<String> = emptyList(),
    @ServerTimestamp val updatedAt: Timestamp? = null
)
