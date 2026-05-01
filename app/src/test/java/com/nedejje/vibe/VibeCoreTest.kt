package com.nedejje.vibe

import android.content.Context
import android.content.SharedPreferences
import com.nedejje.vibe.db.UserEntity
import com.nedejje.vibe.session.SessionManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests covering core application functions as required by the
 * Ndejje University Capstone Project (Component 3.5).
 * Verified by the Testing and QA Engineer.
 */
@RunWith(MockitoJUnitRunner::class)
class VibeCoreTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Test
    fun testPasswordHashing() {
        val app = VibeApplication()
        val password = "Admin@Vibe2025!"
        
        // Ensure hashing is consistent and produces expected SHA-256 hex
        val hash1 = app.hashPassword(password)
        val hash2 = app.hashPassword(password)
        
        assertEquals("Hashing should be deterministic", hash1, hash2)
        assertNotEquals("Hash should not match plain text", password, hash1)
        assertEquals("Hash length should be 64 characters (SHA-256)", 64, hash1.length)
    }

    @Test
    fun testSessionLoginLogic() {
        // Setup mock SharedPreferences
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)

        SessionManager.init(mockContext)
        
        val testUser = UserEntity(
            id = "test-123",
            name = "Test User",
            email = "test@vibe.ug",
            phone = "0700000000",
            isAdmin = false
        )

        // Perform login
        SessionManager.login(testUser)

        // Verify SessionManager state
        assertTrue("User should be logged in after login()", SessionManager.isLoggedIn)
        assertEquals("Session userId should match logged in user", "test-123", SessionManager.userId)
        assertFalse("User should not be admin", SessionManager.isAdmin)

        // Verify SharedPreferences were updated
        verify(mockEditor).putString("user_id", "test-123")
        verify(mockEditor).apply()

        // Perform logout
        SessionManager.logout()
        assertFalse("User should be logged out after logout()", SessionManager.isLoggedIn)
    }
}