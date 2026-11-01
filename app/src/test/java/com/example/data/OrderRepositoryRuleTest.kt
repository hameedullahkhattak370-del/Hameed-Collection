package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class OrderRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun placeOrder_authenticatedCustomer_createsOrderSuccessfully() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = OrderRepository(firestore)

        val sampleOrder = Order(
            id = UUID.randomUUID().toString(),
            orderNumber = "HC-TEST-001",
            customerId = aliceUid,
            customerName = "Alice Khattak",
            customerPhone = "03001234567",
            customerEmail = ALICE_EMAIL,
            city = "Peshawar",
            deliveryAddress = "Saddar Road, House 42",
            deliveryNotes = "Near Saddar Bazaar",
            items = listOf(
                OrderItem(
                    productId = "prod_boski",
                    title = "Royal Boski Suit",
                    price = 5400L,
                    quantity = 1L,
                    selectedSize = "4.25 Meters",
                    selectedColor = "Natural Cream"
                )
            ),
            subtotal = 5400L,
            deliveryFee = 200L,
            totalAmount = 5600L,
            paymentMethod = "COD",
            status = "Pending"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.placeOrder(sampleOrder)
        }

        assertTrue("Order creation should succeed", result.isSuccess)
        val generatedOrderNum = result.getOrThrow()
        assertTrue("Order number should start with HC-", generatedOrderNum.startsWith("HC-"))
    }

    @Test
    fun observeCustomerOrders_authenticatedOwner_returnsCustomerOrders() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = OrderRepository(firestore)

        val uniqueId = UUID.randomUUID().toString()
        val sampleOrder = Order(
            id = uniqueId,
            orderNumber = "HC-TEST-002",
            customerId = aliceUid,
            customerName = "Alice Khattak",
            customerPhone = "03001234567",
            customerEmail = ALICE_EMAIL,
            city = "Islamabad",
            deliveryAddress = "F-7/2, Street 15",
            items = listOf(
                OrderItem(
                    productId = "prod_kurta",
                    title = "Cotton Kurta",
                    price = 3200L,
                    quantity = 1L
                )
            ),
            subtotal = 3200L,
            deliveryFee = 200L,
            totalAmount = 3400L,
            paymentMethod = "COD",
            status = "Pending"
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.placeOrder(sampleOrder).getOrThrow()
        }

        val orders = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCustomerOrders(aliceUid).first { list ->
                list.any { it.customerName == "Alice Khattak" }
            }
        }

        assertTrue(orders.isNotEmpty())
        assertEquals(aliceUid, orders.first().customerId)
    }

    @Test
    fun cancelCustomerOrder_pendingStatus_cancelsOrderSuccessfully() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = OrderRepository(firestore)

        val uniqueId = UUID.randomUUID().toString()
        val sampleOrder = Order(
            id = uniqueId,
            orderNumber = "HC-TEST-003",
            customerId = aliceUid,
            customerName = "Alice Khattak",
            customerPhone = "03001234567",
            customerEmail = ALICE_EMAIL,
            city = "Karachi",
            deliveryAddress = "Clifton Block 4",
            items = listOf(
                OrderItem(
                    productId = "prod_waistcoat",
                    title = "Jacquard Waistcoat",
                    price = 4600L,
                    quantity = 1L
                )
            ),
            subtotal = 4600L,
            deliveryFee = 200L,
            totalAmount = 4800L,
            paymentMethod = "COD",
            status = "Pending"
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.placeOrder(sampleOrder).getOrThrow()
        }

        val cancelResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.cancelCustomerOrder(uniqueId)
        }
        assertTrue("Cancellation should succeed", cancelResult.isSuccess)
    }

    @Test
    fun observeCustomerOrders_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = OrderRepository(firestore)

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.getCustomerOrders("unauth_user_id")
            }
            fail("Expected exception for unauthenticated query")
        } catch (e: Exception) {
            val isPermissionDenied = e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            val isWrappedPermissionDenied = e.cause is FirebaseFirestoreException &&
                (e.cause as FirebaseFirestoreException).code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            val hasPermissionText = e.message?.contains("PERMISSION_DENIED") == true ||
                e.message?.contains("Missing or insufficient permissions") == true
            assertTrue(
                "Expected PERMISSION_DENIED exception but got: ${e.javaClass.name} - ${e.message}",
                isPermissionDenied || isWrappedPermissionDenied || hasPermissionText
            )
        }
    }

    @Test
    fun placeOrder_guestUser_createsOrderSuccessfullyWithoutSignIn() = runBlocking {
        auth.signOut()
        val repository = OrderRepository(firestore)

        val guestOrder = Order(
            customerName = "Guest Shopper",
            customerPhone = "03331234567",
            city = "Lahore",
            area = "Gulberg III",
            deliveryAddress = "Main Boulevard, Plaza 5",
            items = listOf(
                OrderItem(
                    productId = "prod_boski",
                    title = "Royal Boski Suit",
                    price = 4800L,
                    quantity = 1L
                )
            ),
            subtotal = 4800L,
            deliveryFee = 200L,
            totalAmount = 5000L,
            paymentMethod = "COD",
            status = "Pending"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.placeOrder(guestOrder)
        }

        assertTrue("Guest order creation failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val generatedOrderNum = result.getOrThrow()
        assertTrue("Order number should start with HC-", generatedOrderNum.startsWith("HC-"))
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val DEFAULT_TIMEOUT_MS = 8000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
