package com.example.data.session

/**
 * In-memory session manager for guest checkout.
 * Keeps guest contact & delivery information in memory during the active session
 * without persisting sensitive data to unencrypted local storage.
 */
object GuestSessionManager {
    var guestName: String = ""
    var guestPhone: String = ""
    var guestCity: String = "Lahore"
    var guestArea: String = ""
    var guestAddress: String = ""
    var guestNotes: String = ""

    // In-memory record of order numbers placed in the current session
    private val _sessionOrderNumbers = mutableListOf<String>()
    val sessionOrderNumbers: List<String> get() = _sessionOrderNumbers

    fun recordPlacedOrder(orderNumber: String) {
        if (!_sessionOrderNumbers.contains(orderNumber)) {
            _sessionOrderNumbers.add(0, orderNumber)
        }
    }

    fun updateGuestDetails(
        name: String,
        phone: String,
        city: String,
        area: String,
        address: String,
        notes: String = ""
    ) {
        guestName = name
        guestPhone = phone
        guestCity = city.ifBlank { "Lahore" }
        guestArea = area
        guestAddress = address
        guestNotes = notes
    }

    fun clearSession() {
        guestName = ""
        guestPhone = ""
        guestCity = "Lahore"
        guestArea = ""
        guestAddress = ""
        guestNotes = ""
        _sessionOrderNumbers.clear()
    }
}
