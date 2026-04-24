package com.nedejje.vibe.session

import android.content.Context
import android.content.SharedPreferences
import com.nedejje.vibe.db.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lightweight in-process session that persists user identity across
 * app restarts via SharedPreferences.  Swap for DataStore in production.
 */
object SessionManager {

    private const val PREFS_NAME     = "vibe_session"
    private const val KEY_USER_ID    = "user_id"
    private const val KEY_USER_NAME  = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_IS_ADMIN   = "is_admin"

    private lateinit var prefs: SharedPreferences

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    val isLoggedIn get() = _currentUser.value != null
    val isAdmin    get() = _currentUser.value?.isAdmin == true
    val userId     get() = _currentUser.value?.id ?: ""

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_USER_ID, null)
        if (id != null) {
            _currentUser.value = UserEntity(
                id      = id,
                name    = prefs.getString(KEY_USER_NAME,  "") ?: "",
                email   = prefs.getString(KEY_USER_EMAIL, "") ?: "",
                phone   = prefs.getString(KEY_USER_PHONE, "") ?: "",
                isAdmin = prefs.getBoolean(KEY_IS_ADMIN,  false)
            )
        }
    }

    fun login(user: UserEntity) {
        _currentUser.value = user
        prefs.edit()
            .putString(KEY_USER_ID,    user.id)
            .putString(KEY_USER_NAME,  user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_PHONE, user.phone)
            .putBoolean(KEY_IS_ADMIN,  user.isAdmin)
            .apply()
    }

    fun updateProfile(name: String, email: String, phone: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(name = name, email = email, phone = phone)
        prefs.edit()
            .putString(KEY_USER_NAME,  name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_PHONE, phone)
            .apply()
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().clear().apply()
    }
}
