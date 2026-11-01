package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.StoreSettings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class StoreSettingsRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val settingsDoc = db.collection("storeSettings").document("config")

    fun observeSettings(): Flow<StoreSettings> {
        val path = "storeSettings/config"
        return settingsDoc.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(StoreSettings::class.java) ?: StoreSettings()
            } else {
                StoreSettings()
            }
        }.catch { error ->
            if (error is Exception) {
                handleFirestoreError(error, OperationType.GET, path)
            }
            emit(StoreSettings())
        }
    }

    suspend fun updateSettings(settings: StoreSettings): Result<Unit> {
        return try {
            val payload = mapOf(
                "storeName" to settings.storeName,
                "phone" to settings.phone,
                "whatsapp" to settings.whatsapp,
                "deliveryFee" to settings.deliveryFee,
                "freeShippingThreshold" to settings.freeShippingThreshold,
                "announcement" to settings.announcement,
                "bannerUrls" to settings.bannerUrls,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            settingsDoc.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "storeSettings/config")
            Result.failure(e)
        }
    }
}
