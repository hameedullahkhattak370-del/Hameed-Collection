package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.Category
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

class CategoryRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: com.google.firebase.auth.FirebaseAuth = try {
        com.google.firebase.auth.FirebaseAuth.getInstance(db.app)
    } catch (_: Exception) {
        Firebase.auth
    }

    private val categoriesCollection = db.collection("categories")

    /**
     * Real-time stream of all categories (including active and inactive) for admin management.
     */
    fun observeCategories(): Flow<List<Category>> {
        return categoriesCollection.snapshots().map { snapshot ->
            val list = snapshot.documents.mapNotNull { doc ->
                val cat = doc.toObject(Category::class.java)
                cat?.copy(id = if (cat.id.isNotBlank()) cat.id else doc.id)
            }
            if (list.isEmpty()) {
                DEFAULT_CATEGORIES
            } else {
                list.sortedBy { it.name.lowercase(Locale.ROOT) }
            }
        }.catch { error ->
            if (error is Exception) {
                handleFirestoreError(error, OperationType.LIST, "categories")
            }
            emit(DEFAULT_CATEGORIES)
        }
    }

    /**
     * Real-time stream of active categories for customer shopping experience.
     */
    fun observeActiveCategories(): Flow<List<Category>> {
        return observeCategories().map { list ->
            list.filter { it.isActive }
        }
    }

    /**
     * Add a new category with duplicate prevention (case-insensitive & trimmed)
     * and a stable generated document ID.
     */
    suspend fun addCategory(
        name: String,
        imageUrl: String = "",
        isActive: Boolean = true
    ): Result<String> {
        return try {
            val trimmedName = name.trim()
            require(trimmedName.length >= 2) { "Category name must be at least 2 characters long" }

            // Check for duplicate category name (case-insensitive)
            val currentSnapshot = categoriesCollection.get().await()
            val isDuplicate = currentSnapshot.documents.any { doc ->
                val existingName = doc.getString("name")?.trim().orEmpty()
                existingName.equals(trimmedName, ignoreCase = true)
            }
            if (isDuplicate) {
                return Result.failure(IllegalArgumentException("A category with the name '$trimmedName' already exists."))
            }

            // Generate stable, clean document ID
            val slug = trimmedName
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .take(30)
            val stableId = if (slug.isNotBlank()) "cat_$slug" else "cat_${UUID.randomUUID().toString().take(8)}"

            // If ID already taken, append unique suffix
            val existingDoc = categoriesCollection.document(stableId).get().await()
            val finalId = if (existingDoc.exists()) "${stableId}_${System.currentTimeMillis() % 10000}" else stableId

            val payload = mapOf(
                "id" to finalId,
                "name" to trimmedName,
                "imageUrl" to imageUrl.trim(),
                "isActive" to isActive,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            categoriesCollection.document(finalId).set(payload).await()
            Result.success(finalId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "categories")
            Result.failure(e)
        }
    }

    /**
     * Edit an existing category name, image URL, and active state.
     */
    suspend fun updateCategory(
        id: String,
        name: String,
        imageUrl: String = "",
        isActive: Boolean = true
    ): Result<Unit> {
        return try {
            val trimmedName = name.trim()
            require(trimmedName.length >= 2) { "Category name must be at least 2 characters long" }
            require(id.isNotBlank()) { "Category ID must not be blank" }

            // Check for duplicate name in other documents
            val currentSnapshot = categoriesCollection.get().await()
            val isDuplicate = currentSnapshot.documents.any { doc ->
                doc.id != id && doc.getString("name")?.trim().equals(trimmedName, ignoreCase = true)
            }
            if (isDuplicate) {
                return Result.failure(IllegalArgumentException("Another category with the name '$trimmedName' already exists."))
            }

            val docRef = categoriesCollection.document(id)
            val existingDoc = docRef.get().await()
            val createdAt = existingDoc.getTimestamp("createdAt")

            val payload = mapOf(
                "id" to id,
                "name" to trimmedName,
                "imageUrl" to imageUrl.trim(),
                "isActive" to isActive,
                "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "categories/$id")
            Result.failure(e)
        }
    }

    /**
     * Delete a category only after confirming no products are assigned to it.
     */
    suspend fun deleteCategory(categoryId: String, isAssignedToAnyProduct: Boolean): Result<Unit> {
        return try {
            require(categoryId.isNotBlank()) { "Category ID must not be blank" }
            if (isAssignedToAnyProduct) {
                return Result.failure(
                    IllegalStateException("Cannot delete this category because active products are assigned to it. Please reassign or delete those products first.")
                )
            }
            categoriesCollection.document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "categories/$categoryId")
            Result.failure(e)
        }
    }

    /**
     * Seeds initial default categories if the collection is empty and the user is an admin.
     */
    suspend fun seedInitialCategoriesIfEmpty() {
        try {
            val user = auth.currentUser ?: return
            val isAdmin = user.email.equals("hameedullahkhattak370@gmail.com", ignoreCase = true)
            if (!isAdmin) return

            val snapshot = categoriesCollection.limit(1).get().await()
            if (!snapshot.isEmpty) return

            for (cat in DEFAULT_CATEGORIES) {
                val payload = mapOf(
                    "id" to cat.id,
                    "name" to cat.name,
                    "imageUrl" to cat.imageUrl,
                    "isActive" to cat.isActive,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                categoriesCollection.document(cat.id).set(payload).await()
            }
        } catch (_: Exception) {
            // Non-blocking fallback
        }
    }

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(
                id = "cat_mens_unstitched",
                name = "Men's Unstitched",
                imageUrl = "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=800&auto=format&fit=crop&q=80",
                isActive = true
            ),
            Category(
                id = "cat_kurta_collection",
                name = "Kurta Collection",
                imageUrl = "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&auto=format&fit=crop&q=80",
                isActive = true
            ),
            Category(
                id = "cat_waistcoats",
                name = "Waistcoats",
                imageUrl = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&auto=format&fit=crop&q=80",
                isActive = true
            ),
            Category(
                id = "cat_shawls",
                name = "Shawls",
                imageUrl = "https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?w=800&auto=format&fit=crop&q=80",
                isActive = true
            )
        )
    }
}
