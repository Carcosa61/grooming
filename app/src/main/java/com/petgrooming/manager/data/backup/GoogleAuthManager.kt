package com.petgrooming.manager.data.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps Google Sign-In configured with the Drive file scope used for backups.
 *
 * Requires an OAuth 2.0 client ID (Android type) configured in Google Cloud Console
 * for the application package and signing certificate.
 */
@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val driveScope: Scope = Scope(DriveScopes.DRIVE_FILE)

    private val client: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(driveScope)
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val signInIntent: Intent
        get() = client.signInIntent

    /** Returns the previously signed-in account only if it still has the Drive scope granted. */
    fun currentAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)
            ?.takeIf { GoogleSignIn.hasPermissions(it, driveScope) }

    fun signOut() {
        client.signOut()
    }
}
