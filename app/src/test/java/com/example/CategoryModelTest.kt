package com.example

import com.example.data.model.Category
import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CategoryModelTest {

    @Test
    fun testCategoryCreation_defaultValues() {
        val category = Category(
            id = "cat_mens_unstitched",
            name = "Men's Unstitched",
            imageUrl = "https://example.com/img.jpg",
            isActive = true
        )

        assertEquals("cat_mens_unstitched", category.id)
        assertEquals("Men's Unstitched", category.name)
        assertTrue(category.isActive)
        assertEquals("https://example.com/img.jpg", category.imageUrl)
    }

    @Test
    fun testDuplicateCategoryDetection_caseInsensitiveAndTrimmed() {
        val existingNames = listOf("Men's Unstitched", "Kurta Collection", "Waistcoats")
        val newCandidate = "  men's unstitched  "

        val isDuplicate = existingNames.any { existing ->
            existing.trim().equals(newCandidate.trim(), ignoreCase = true)
        }

        assertTrue("Should detect case-insensitive duplicate", isDuplicate)

        val uniqueCandidate = "  Shawls & Stoles  "
        val isUniqueDuplicate = existingNames.any { existing ->
            existing.trim().equals(uniqueCandidate.trim(), ignoreCase = true)
        }
        assertFalse("Unique candidate should not be marked duplicate", isUniqueDuplicate)
    }

    @Test
    fun testCategorySlugGeneration() {
        val name = "Men's Luxury Boski Suits!"
        val slug = name.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(30)
        val stableId = "cat_$slug"

        assertEquals("cat_men_s_luxury_boski_suits", stableId)
    }

    @Test
    fun testProductEffectiveImageUrlFallback() {
        val productWithUrl = Product(
            id = "p1",
            title = "Test Product",
            imageUrl = "https://storage.googleapis.com/test.jpg"
        )
        assertEquals("https://storage.googleapis.com/test.jpg", productWithUrl.effectiveImageUrl)

        val productWithImagesList = Product(
            id = "p2",
            title = "Test Product 2",
            images = listOf("https://storage.googleapis.com/img1.jpg", "https://storage.googleapis.com/img2.jpg")
        )
        assertEquals("https://storage.googleapis.com/img1.jpg", productWithImagesList.effectiveImageUrl)
    }

    @Test
    fun testCategoryProductAssignmentCheck() {
        val products = listOf(
            Product(id = "p1", title = "Kurta", categoryId = "cat_kurta", category = "Kurta Collection"),
            Product(id = "p2", title = "Waistcoat", categoryId = "cat_waistcoats", category = "Waistcoats")
        )

        val targetCatId = "cat_kurta"
        val hasAssignedProducts = products.any { it.categoryId == targetCatId || it.category.equals("Kurta Collection", ignoreCase = true) }
        assertTrue("Category should have assigned products", hasAssignedProducts)

        val emptyCatId = "cat_shawls"
        val hasShawlProducts = products.any { it.categoryId == emptyCatId || it.category.equals("Shawls", ignoreCase = true) }
        assertFalse("Category should not have assigned products", hasShawlProducts)
    }
}
