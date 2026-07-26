package com.prog7313.sandbox.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.prog7313.sandbox.R

class GoogleAuthClient(
    private val appContext: Context
) {
    private val credentialManager = CredentialManager.create(appContext)

    suspend fun getGoogleIdToken(activityContext: Context): Result<String> {
        return try {
            val googleOption = GetSignInWithGoogleOption.Builder(
                appContext.getString(R.string.default_web_client_id)
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

            val response = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = response.credential

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                Result.success(googleCredential.idToken)
            } else {
                Result.failure(Exception("A Google ID credential was not returned"))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun clearCredentialState() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
    }
}