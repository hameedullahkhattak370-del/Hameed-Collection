package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.Product
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(private val db: FirebaseFirestore) {

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

    private val productsCollection = db.collection("products")

    fun observeProducts(): Flow<List<Product>> {
        return productsCollection.snapshots().map { snapshot ->
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val p = doc.toObject(Product::class.java)
                    p?.copy(id = if (p.id.isNotBlank()) p.id else doc.id)
                } catch (_: Exception) {
                    parseProductDocument(doc)
                }
            }
            if (list.isEmpty()) {
                DEFAULT_PRODUCTS
            } else {
                list
            }
        }.catch { error ->
            if (error is Exception) {
                handleFirestoreError(error, OperationType.LIST, "products")
            }
            emit(DEFAULT_PRODUCTS)
        }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val doc = productsCollection.document(productId).get().await()
            if (doc.exists()) {
                val p = doc.toObject(Product::class.java)
                p?.copy(id = if (p.id.isNotBlank()) p.id else doc.id)
                    ?: parseProductDocument(doc)
                    ?: DEFAULT_PRODUCTS.find { it.id == productId }
            } else {
                DEFAULT_PRODUCTS.find { it.id == productId }
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "products/$productId")
            DEFAULT_PRODUCTS.find { it.id == productId }
        }
    }

    private fun parseProductDocument(doc: com.google.firebase.firestore.DocumentSnapshot): Product? {
        return try {
            val title = doc.getString("title") ?: return null
            val description = doc.getString("description") ?: ""
            val category = doc.getString("category") ?: ""
            val categoryId = doc.getString("categoryId") ?: ""
            val price = doc.getLong("price") ?: 0L
            val salePrice = doc.getLong("salePrice") ?: 0L
            val stock = doc.getLong("stock") ?: 0L
            val imageUrl = doc.getString("imageUrl") ?: ""
            @Suppress("UNCHECKED_CAST")
            val images = (doc.get("images") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val sizes = (doc.get("sizes") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val colors = (doc.get("colors") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val isFeatured = doc.getBoolean("isFeatured") ?: false
            val isNewArrival = doc.getBoolean("isNewArrival") ?: false
            val isArchived = doc.getBoolean("isArchived") ?: false
            val isActive = doc.getBoolean("isActive") ?: true
            val isAvailable = doc.getBoolean("isAvailable") ?: true
            val createdAt = doc.getTimestamp("createdAt")
            val updatedAt = doc.getTimestamp("updatedAt")

            Product(
                id = doc.id,
                title = title,
                description = description,
                categoryId = categoryId,
                category = category,
                price = price,
                salePrice = salePrice,
                stock = stock,
                images = images,
                imageUrl = imageUrl,
                sizes = sizes,
                colors = colors,
                isFeatured = isFeatured,
                isNewArrival = isNewArrival,
                isArchived = isArchived,
                isActive = isActive,
                isAvailable = isAvailable,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val docId = if (product.id.isNotBlank()) product.id else UUID.randomUUID().toString()
            val effectiveImg = product.effectiveImageUrl
            val imgList = if (product.images.isNotEmpty()) product.images else if (effectiveImg.isNotBlank()) listOf(effectiveImg) else emptyList()
            val payload = mapOf(
                "id" to docId,
                "title" to product.title,
                "description" to product.description,
                "category" to product.category,
                "categoryId" to product.categoryId,
                "price" to product.price,
                "salePrice" to product.salePrice,
                "stock" to product.stock,
                "images" to imgList,
                "imageUrl" to effectiveImg,
                "sizes" to product.sizes,
                "colors" to product.colors,
                "isFeatured" to product.isFeatured,
                "isNewArrival" to product.isNewArrival,
                "isArchived" to product.isArchived,
                "isActive" to product.isActive,
                "isAvailable" to product.isAvailable,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            productsCollection.document(docId).set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "products")
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            require(product.id.isNotBlank()) { "Product ID must not be blank" }
            val existingDoc = productsCollection.document(product.id).get().await()
            val createdAt = existingDoc.getTimestamp("createdAt")
            val effectiveImg = product.effectiveImageUrl
            val imgList = if (product.images.isNotEmpty()) product.images else if (effectiveImg.isNotBlank()) listOf(effectiveImg) else emptyList()

            val payload = mapOf(
                "id" to product.id,
                "title" to product.title,
                "description" to product.description,
                "category" to product.category,
                "categoryId" to product.categoryId,
                "price" to product.price,
                "salePrice" to product.salePrice,
                "stock" to product.stock,
                "images" to imgList,
                "imageUrl" to effectiveImg,
                "sizes" to product.sizes,
                "colors" to product.colors,
                "isFeatured" to product.isFeatured,
                "isNewArrival" to product.isNewArrival,
                "isArchived" to product.isArchived,
                "isActive" to product.isActive,
                "isAvailable" to product.isAvailable,
                "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            productsCollection.document(product.id).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "products/${product.id}")
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "products/$productId")
            Result.failure(e)
        }
    }

    suspend fun seedInitialCatalogIfEmpty(force: Boolean = false) {
        try {
            val user = auth.currentUser ?: return
            val isAdmin = user.email.equals("hameedullahkhattak370@gmail.com", ignoreCase = true)
            if (!isAdmin) {
                return
            }

            if (!force) {
                val snapshot = productsCollection.limit(1).get().await()
                if (!snapshot.isEmpty) {
                    return
                }
            }

            for (p in DEFAULT_PRODUCTS) {
                addProduct(p)
            }
        } catch (e: Exception) {
            // Non-critical seed
        }
    }

    companion object {
        val DEFAULT_PRODUCTS = listOf(
            Product(
                id = "prod_boski_white",
                title = "Royal Boski Unstitched Luxury Suit",
                description = "Heritage Pakistani micro-modal blended Boski fabric with smooth drape, soft sheen, and unmatched comfort for formal and festive occasions. Includes 4.25 meters fabric and presentation box.",
                categoryId = "cat_mens_unstitched",
                category = "Men's Unstitched",
                price = 6500L,
                salePrice = 5400L,
                stock = 35L,
                images = listOf("https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=800&auto=format&fit=crop&q=80"),
                imageUrl = "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=800&auto=format&fit=crop&q=80",
                sizes = listOf("4.25 Meters (Standard)"),
                colors = listOf("Natural Cream", "Pure White", "Ivory"),
                isFeatured = true,
                isNewArrival = true,
                isAvailable = true
            ),
            Product(
                id = "prod_washwear_charcoal",
                title = "Executive Charcoal Wash & Wear Suit",
                description = "Wrinkle-resistant fine tropical blended fabric crafted for all-day sharpness in Pakistani summers and transitional weather. Premium fall, breathable texture.",
                categoryId = "cat_mens_unstitched",
                category = "Men's Unstitched",
                price = 4800L,
                salePrice = 3950L,
                stock = 42L,
                images = listOf("https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?w=800&auto=format&fit=crop&q=80"),
                imageUrl = "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?w=800&auto=format&fit=crop&q=80",
                sizes = listOf("4.25 Meters (Standard)"),
                colors = listOf("Charcoal Grey", "Jet Black", "Deep Navy"),
                isFeatured = true,
                isNewArrival = true,
                isAvailable = true
            ),
            Product(
                id = "prod_kurta_embroidered_olive",
                title = "Embroidered Festive Cotton Kurta",
                description = "Pure combed cotton kurta featuring intricate Pakistani thread embroidery on ban collar and front placket. Tailored slim-contemporary fit with mother-of-pearl buttons.",
                categoryId = "cat_kurta_collection",
                category = "Kurta Collection",
                price = 3800L,
                salePrice = 3200L,
                stock = 28L,
                images = listOf("https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&auto=format&fit=crop&q=80"),
                imageUrl = "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&auto=format&fit=crop&q=80",
                sizes = listOf("S (38)", "M (40)", "L (42)", "XL (44)"),
                colors = listOf("Olive Green", "Midnight Blue", "Sand Beige"),
                isFeatured = true,
                isNewArrival = false,
                isAvailable = true
            ),
            Product(
                id = "prod_waistcoat_jacquard",
                title = "Heritage Jamawar Jacquard Waistcoat",
                description = "Opulent ban-collar waistcoat in subtle gold-threaded jacquard. Ideal layer over kurtas for wedding receptions, Eid gatherings and formal celebrations.",
                categoryId = "cat_waistcoats",
                category = "Waistcoats",
                price = 5500L,
                salePrice = 4600L,
                stock = 18L,
                images = listOf("https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&auto=format&fit=crop&q=80"),
                imageUrl = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&auto=format&fit=crop&q=80",
                sizes = listOf("38", "40", "42", "44"),
                colors = listOf("Black Gold", "Maroon Gold", "Emerald Gold"),
                isFeatured = true,
                isNewArrival = true,
                isAvailable = true
            ),
            Product(
                id = "prod_shawl_pashmina_blend",
                title = "Kashmiri Embroidered Doshala Shawl",
                description = "Soft wool-pashmina blended men's winter shawl adorned with traditional hand-guided paisley border embroidery. Timeless elegance and warmth.",
                categoryId = "cat_shawls",
                category = "Shawls",
                price = 8500L,
                salePrice = 7200L,
                stock = 15L,
                images = listOf("https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?w=800&auto=format&fit=crop&q=80"),
                imageUrl = "https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?w=800&auto=format&fit=crop&q=80",
                sizes = listOf("Full Size (2.5m x 1.25m)"),
                colors = listOf("Camel Brown", "Deep Charcoal", "Charcoal Black"),
                isFeatured = false,
                isNewArrival = true,
                isAvailable = true
            ),
            Product(
                id = "prod_linen_kurta_slate",
                title = "Everyday Casual Slub Cotton Kurta",
                description = "Airy slub cotton fabric for daily comfort, casual meetings, and Friday prayers. Features round cuff sleeves and two side pockets.",
                categoryId = "cat_kurta_collection",
                category = "Kurta Collection",
                price = 2800L,
                salePrice = 2400L,
                stock = 50L,
                images = listOf("https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=800&auto=format&fit=crop&q=80"),
                imageUrl = "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=800&auto=format&fit=crop&q=80",
                sizes = listOf("S", "M", "L", "XL"),
                colors = listOf("Slate Blue", "Pearl Grey", "Off-White"),
                isFeatured = false,
                isNewArrival = false,
                isAvailable = true
            )
        )
    }
}
