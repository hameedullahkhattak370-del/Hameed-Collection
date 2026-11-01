package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val usersCollection = db.collection("users")

    fun observeProfile(userId: String): Flow<UserProfile?> {
        if (userId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(null)
        }
        val path = "users/$userId"
        return usersCollection.document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, path)
                }
                throw error
            }
    }

    suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not authenticated"))
        return try {
            val docRef = usersCollection.document(uid)
            val existing = docRef.get().await()
            val createdAt = existing.getTimestamp("createdAt")

            val payload = mapOf(
                "userId" to uid,
                "name" to profile.name.trim(),
                "email" to (profile.email.ifBlank { auth.currentUser?.email ?: "" }),
                "phone" to profile.phone.trim(),
                "city" to profile.city.trim(),
                "area" to profile.area.trim(),
                "postalCode" to profile.postalCode.trim(),
                "address" to profile.address.trim(),
                "role" to (if (existing.getString("role") == "admin" || auth.currentUser?.email == "hameedullahkhattak370@gmail.com") "admin" else "customer"),
                "wishlist" to profile.wishlist,
                "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun toggleWishlist(productId: String): Result<Boolean> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not authenticated"))
        return try {
            val docRef = usersCollection.document(uid)
            val doc = docRef.get().await()
            val profile = doc.toObject(UserProfile::class.java) ?: UserProfile(
                userId = uid,
                name = auth.currentUser?.displayName ?: "Valued Customer",
                email = auth.currentUser?.email ?: ""
            )
            val currentList = profile.wishlist.toMutableList()
            val isNowFavorite = if (currentList.contains(productId)) {
                currentList.remove(productId)
                false
            } else {
                currentList.add(productId)
                true
            }

            saveProfile(profile.copy(wishlist = currentList))
            Result.success(isNowFavorite)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
            Result.failure(e)
        }
    }
}
