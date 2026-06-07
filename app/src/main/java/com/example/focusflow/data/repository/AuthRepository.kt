package com.example.focusflow.data.repository

import com.example.focusflow.data.local.AppDatabase
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val database: AppDatabase
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        firebaseAuth.signOut()
        try {
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            // Error al cerrar sesión en Google, pero Firebase ya se cerró
        }
        withContext(Dispatchers.IO) {
            database.clearAllTables()
            try {
                database.openHelper.writableDatabase.execSQL("DELETE FROM sqlite_sequence")
            } catch (e: Exception) {
                // Opcional: manejar error si la tabla no existe
            }
        }
    }

    fun getUserId(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }

    fun getUserEmail(): String {
        return firebaseAuth.currentUser?.email ?: ""
    }

    fun getSanitizedEmail(): String {
        return getUserEmail().replace(".", "_")
    }

    fun getUserName(): String {
        return firebaseAuth.currentUser?.displayName ?: "Usuario"
    }

    fun getUserPhotoUrl(): String {
        return firebaseAuth.currentUser?.photoUrl?.toString() ?: ""
    }

    fun getUserEmail(): String {
        return firebaseAuth.currentUser?.email ?: ""
    }
}
