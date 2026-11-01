package com.example.data.cart

import com.example.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selectedSize: String = "",
    val selectedColor: String = ""
) {
    val unitPrice: Long
        get() = product.displayPrice

    val totalPrice: Long
        get() = unitPrice * quantity
}

object CartManager {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addItem(product: Product, size: String, color: String, quantity: Int = 1) {
        _items.update { currentList ->
            val existingIndex = currentList.indexOfFirst {
                it.product.id == product.id && it.selectedSize == size && it.selectedColor == color
            }
            if (existingIndex >= 0) {
                currentList.mapIndexed { index, item ->
                    if (index == existingIndex) {
                        item.copy(quantity = item.quantity + quantity)
                    } else item
                }
            } else {
                currentList + CartItem(
                    product = product,
                    quantity = quantity,
                    selectedSize = size,
                    selectedColor = color
                )
            }
        }
    }

    fun updateQuantity(productId: String, size: String, color: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(productId, size, color)
        } else {
            _items.update { currentList ->
                currentList.map { item ->
                    if (item.product.id == productId && item.selectedSize == size && item.selectedColor == color) {
                        item.copy(quantity = newQuantity)
                    } else item
                }
            }
        }
    }

    fun removeItem(productId: String, size: String, color: String) {
        _items.update { currentList ->
            currentList.filterNot {
                it.product.id == productId && it.selectedSize == size && it.selectedColor == color
            }
        }
    }

    fun clearCart() {
        _items.value = emptyList()
    }

    fun getSubtotal(): Long {
        return _items.value.sumOf { it.totalPrice }
    }

    fun getTotalCount(): Int {
        return _items.value.sumOf { it.quantity }
    }
}

fun formatPkr(amount: Long): String {
    return String.format(java.util.Locale.US, "Rs. %,d", amount)
}
