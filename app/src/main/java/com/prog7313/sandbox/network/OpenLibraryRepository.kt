package com.prog7313.sandbox.network.openlibrary

import com.prog7313.sandbox.model.openlibrary.OpenLibrarySearchResponse

object OpenLibraryRepository {

    suspend fun searchBooks(
        query: String
    ): OpenLibrarySearchResponse {

        val response =
            RetrofitClient.openLibraryApi.searchBooks(
                query = query
            )

        if (!response.isSuccessful) {
            throw IllegalStateException(
                "Open Library returned error ${response.code()}."
            )
        }

        return response.body()
            ?: throw IllegalStateException(
                "Open Library returned an empty response."
            )
    }
}