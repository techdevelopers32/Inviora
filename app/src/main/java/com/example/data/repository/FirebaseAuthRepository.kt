package com.example.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Repository responsible for managing Firebase Anonymous Authentication.
 *
 * Provides a persistent authenticated identity for the Android Inviora app
 * so that Firestore security rules can distinguish the host app from guest viewers.
 *
 * - Checks whether FirebaseAuth.currentUser already exists.
 * - Reuses the existing user/UID across app launches without creating redundant accounts.
 * - If not authenticated, signs in anonymously via Firebase Auth.
 * - Fails safely without disrupting local Room database operations or local editing.
 */
open class FirebaseAuthRepository(
  private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() },
  private val currentUserIdProvider: (() -> String?)? = null,
  private val anonymousSignInAction: (suspend () -> String)? = null
) {
  companion object {
    private const val TAG = "FirebaseAuthRepository"
  }

  /**
   * Checks whether a user is currently signed in.
   */
  open fun isAuthenticated(): Boolean {
    return try {
      if (currentUserIdProvider != null) {
        currentUserIdProvider.invoke() != null
      } else {
        authProvider().currentUser != null
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error checking authentication status: ${e.message}")
      false
    }
  }

  /**
   * Returns the current Firebase user's UID, or null if not authenticated.
   */
  open fun getCurrentUserId(): String? {
    return try {
      if (currentUserIdProvider != null) {
        currentUserIdProvider.invoke()
      } else {
        authProvider().currentUser?.uid
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error retrieving current user UID: ${e.message}")
      null
    }
  }

  /**
   * Ensures that the Android app has an authenticated identity:
   * 1. Checks whether FirebaseAuth.currentUser already exists.
   * 2. If it exists, returns successfully with the existing UID (reusing anonymous credentials).
   * 3. If no user exists, signs in anonymously.
   * 4. Returns the Firebase anonymous user UID.
   * 5. Properly catches and reports authentication errors via Result<String>.
   */
  open suspend fun ensureAuthenticated(): Result<String> {
    return try {
      val existingUid = if (currentUserIdProvider != null) {
        currentUserIdProvider.invoke()
      } else {
        authProvider().currentUser?.uid
      }

      if (!existingUid.isNullOrBlank()) {
        Log.d(TAG, "Reusing existing authenticated user UID: $existingUid")
        return Result.success(existingUid)
      }

      Log.d(TAG, "No authenticated user found. Signing in anonymously...")
      val uid = if (anonymousSignInAction != null) {
        anonymousSignInAction.invoke()
      } else {
        val result = authProvider().signInAnonymously().await()
        val user = result.user
          ?: return Result.failure(IllegalStateException("Firebase anonymous sign-in completed but currentUser was null."))
        user.uid
      }

      Log.d(TAG, "Successfully authenticated anonymously with UID: $uid")
      Result.success(uid)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to authenticate anonymously with Firebase: ${e.message}", e)
      Result.failure(e)
    }
  }
}
