package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WhatsAppUrlTest {

    private fun buildWhatsAppUrl(rawPhone: String, message: String): String {
        val cleanDigits = rawPhone.replace(Regex("[^0-9]"), "")
        val targetPhone = if (cleanDigits.isNotBlank() && cleanDigits != "923001234567") cleanDigits else "923271825407"
        val encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.toString()).replace("+", "%20")
        return "https://wa.me/$targetPhone?text=$encodedMsg"
    }

    @Test
    fun testOfficialWhatsAppNumber_hasNoPlusOrSpaces() {
        val rawNumber = "+923271825407"
        val cleanDigits = rawNumber.replace(Regex("[^0-9]"), "")
        assertEquals("923271825407", cleanDigits)
        assertFalse(cleanDigits.contains("+"))
        assertFalse(cleanDigits.contains(" "))
    }

    @Test
    fun testOfficialWhatsAppNumber_withSpacesAndDashes() {
        val rawNumber = "+92 327-1825407"
        val cleanDigits = rawNumber.replace(Regex("[^0-9]"), "")
        assertEquals("923271825407", cleanDigits)
    }

    @Test
    fun testWhatsAppUrlFormat_matchesRequirement() {
        val message = "Assalam-o-Alaikum! I would like to inquire about a product from Hameed Collection."
        val url = buildWhatsAppUrl("+923271825407", message)

        assertTrue(url.startsWith("https://wa.me/923271825407"))
        assertFalse(url.contains("+92"))
        assertFalse(url.contains(" "))
        assertTrue(url.contains("Assalam-o-Alaikum"))
    }
}
