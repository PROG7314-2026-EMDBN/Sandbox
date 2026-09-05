package com.prog7313.sandbox.network.openlibrary

import com.prog7313.sandbox.model.openlibrary.OpenLibrarySearchResponse
import retrofit2.Response

object OpenLibraryRepository {

    fun normaliseQuery(query: String): String {
        return query.trim()
    }

    fun isValidQuery(query: String): Boolean {
        return normaliseQuery(query).isNotBlank()
    }

    internal fun resolveResponse(
        response: Response<OpenLibrarySearchResponse>
    ): OpenLibrarySearchResponse {
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

    suspend fun searchBooks(query: String): OpenLibrarySearchResponse {
        return searchBooks(
            api = RetrofitClient.openLibraryApi,
            query = query
        )
    }

    internal suspend fun searchBooks(
        api: OpenLibraryApi,
        query: String
    ): OpenLibrarySearchResponse {
        val cleanQuery = normaliseQuery(query)

        require(isValidQuery(cleanQuery)) {
            "Search query cannot be blank."
        }

        val response = api.searchBooks(query = cleanQuery)
        return resolveResponse(response)
    }
}
