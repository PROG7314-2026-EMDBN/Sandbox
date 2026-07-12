package com.prog7313.sandbox.network.openlibrary

import com.prog7313.sandbox.model.openlibrary.OpenLibrarySearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q")
        query: String,

        @Query("limit")
        limit: Int = 5,

        @Query("fields")
        fields: String =
            "key,title,author_name,first_publish_year"
    ): Response<OpenLibrarySearchResponse>
}