package com.prog7313.sandbox.model.openlibrary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenLibrarySearchResponse(
    @SerialName("numFound")
    val numberFound: Int = 0,

    val docs: List<OpenLibraryBookDto> = emptyList()
)

@Serializable
data class OpenLibraryBookDto(
    val key: String = "",

    val title: String = "Untitled",

    @SerialName("author_name")
    val authorNames: List<String> = emptyList(),

    @SerialName("first_publish_year")
    val firstPublishYear: Int? = null
)