package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.Order
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class OrderRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: com.google.firebase.auth.FirebaseAuth = try {
        com.google.firebase.auth.FirebaseAuth.getInstance(db.app)
    } catch (e: Exception) {
        Firebase.auth
    }
    private val ordersCollection = db.collection("orders")

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before placing an order.")
    }

    suspend fun getCustomerOrders(customerId: String): List<Order> {
        val path = "orders"
        return try {
            ordersCollection.whereEqualTo("customerId", customerId).get().await()
                .toObjects(Order::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    fun observeCustomerOrders(customerId: String): Flow<List<Order>> {
        if (customerId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val path = "orders"
        return ordersCollection
            .whereEqualTo("customerId", customerId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeAllOrdersForAdmin(): Flow<List<Order>> {
        val path = "orders"
        return ordersCollection
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun placeOrder(order: Order): Result<String> {
        val currentUser = auth.currentUser
        val isGuest = currentUser == null
        val customerId = if (currentUser != null) {
            currentUser.uid
        } else {
            if (order.customerId.isNotBlank() && order.customerId.startsWith("guest_")) {
                order.customerId
            } else {
                "guest_" + UUID.randomUUID().toString().replace("-", "").take(16)
            }
        }

        return try {
            val docId = if (order.id.isNotBlank()) order.id else UUID.randomUUID().toString()
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
            val dateStr = dateFormat.format(Date())
            val randomSuffix = (1000..9999).random()
            val generatedOrderNumber = "HC-$dateStr-$randomSuffix"

            val itemsPayload = order.items.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "title" to item.title,
                    "price" to item.price,
                    "quantity" to item.quantity,
                    "selectedSize" to item.selectedSize,
                    "selectedColor" to item.selectedColor,
                    "imageUrl" to item.imageUrl
                )
            }

            val payload = mapOf(
                "orderNumber" to generatedOrderNumber,
                "customerId" to customerId,
                "customerName" to order.customerName.trim(),
                "customerPhone" to order.customerPhone.trim(),
                "customerEmail" to (order.customerEmail.ifBlank { currentUser?.email ?: "" }),
                "city" to order.city.trim(),
                "area" to order.area.trim(),
                "deliveryAddress" to order.deliveryAddress.trim(),
                "deliveryNotes" to order.deliveryNotes.trim(),
                "items" to itemsPayload,
                "subtotal" to order.subtotal,
                "deliveryFee" to order.deliveryFee,
                "totalAmount" to order.totalAmount,
                "paymentMethod" to "COD",
                "paymentStatus" to "Unpaid",
                "isGuest" to isGuest,
                "status" to "Pending",
                "courierName" to "",
                "trackingNumber" to "",
                "commissionRate" to order.commissionRate,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            ordersCollection.document(docId).set(payload).await()
            if (isGuest) {
                com.example.data.session.GuestSessionManager.recordPlacedOrder(generatedOrderNumber)
            }
            Result.success(generatedOrderNumber)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "orders")
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        courierName: String = "",
        trackingNumber: String = ""
    ): Result<Unit> {
        return try {
            val docRef = ordersCollection.document(orderId)
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (courierName.isNotBlank()) {
                updates["courierName"] = courierName.trim()
            }
            if (trackingNumber.isNotBlank()) {
                updates["trackingNumber"] = trackingNumber.trim()
            }
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "orders/$orderId")
            Result.failure(e)
        }
    }

    suspend fun cancelCustomerOrder(orderId: String): Result<Unit> {
        val uid = requireUserId()
        return try {
            val docRef = ordersCollection.document(orderId)
            val existing = docRef.get().await().toObject(Order::class.java)
                ?: throw IllegalStateException("Order not found")
            if (existing.customerId != uid) {
                throw IllegalStateException("Cannot cancel another user's order")
            }
            if (existing.status != "Pending") {
                throw IllegalStateException("Only Pending orders can be cancelled")
            }
            docRef.update(
                mapOf(
                    "status" to "Cancelled",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "orders/$orderId")
            Result.failure(e)
        }
    }
}
