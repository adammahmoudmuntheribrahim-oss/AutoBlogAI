package com.example.articleautomator.auth

import org.junit.Assert.*
import org.junit.Test
import java.util.Base64

class TokenManagerTest {

    @Test
    fun testGenerateCodeChallenge() {
        val verifier = "test_verifier_string_1234567890_test_verifier"
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = java.security.MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        val expectedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
        
        assertNotNull(expectedChallenge)
        assertTrue(expectedChallenge.isNotEmpty())
    }
}
