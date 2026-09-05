package com.prog7313.sandbox.network

import com.prog7313.sandbox.model.openlibrary.OpenLibraryBookDto
import com.prog7313.sandbox.model.openlibrary.OpenLibrarySearchResponse
import com.prog7313.sandbox.network.openlibrary.OpenLibraryRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class OpenLibraryRepositoryTest {

    @Test
    fun normaliseQuery_trimsWhitespace() {
        val result =
            OpenLibraryRepository.normaliseQuery(
                "  clean code  "
            )

        assertEquals(
            "clean code",
            result
        )
    }

    @Test
    fun isValidQuery_blankQuery_returnsFalse() {
        assertFalse(
            OpenLibraryRepository.isValidQuery(
                "   "
            )
        )
    }

    @Test
    fun isValidQuery_normalQuery_returnsTrue() {
        assertTrue(
            OpenLibraryRepository.isValidQuery(
                "Kotlin"
            )
        )
    }

    @Test
    fun resolveResponse_successfulResponse_returnsBody() {
        val expected =
            OpenLibrarySearchResponse(
                numberFound = 1,
                docs = listOf(
                    OpenLibraryBookDto(
                        key = "/works/OL1W",
                        title = "Clean Code",
                        authorNames =
                            listOf("Robert C. Martin"),
                        firstPublishYear = 2008
                    )
                )
            )

        val response =
            Response.success(expected)

        val result =
            OpenLibraryRepository
                .resolveResponse(response)

        assertEquals(
            expected,
            result
        )
    }

    @Test
    fun resolveResponse_unsuccessfulResponse_throwsException() {
        val response =
            Response.error<OpenLibrarySearchResponse>(
                503,
                "{}".toResponseBody(
                    "application/json".toMediaType()
                )
            )

        val exception =
            assertThrows(
                IllegalStateException::class.java
            ) {
                OpenLibraryRepository
                    .resolveResponse(response)
            }

        assertEquals(
            "Open Library returned error 503.",
            exception.message
        )
    }

    @Test
    fun resolveResponse_emptyBody_throwsException() {
        val response =
            Response.success<OpenLibrarySearchResponse>(
                null
            )

        val exception =
            assertThrows(
                IllegalStateException::class.java
            ) {
                OpenLibraryRepository
                    .resolveResponse(response)
            }

        assertEquals(
            "Open Library returned an empty response.",
            exception.message
        )
    }
}
