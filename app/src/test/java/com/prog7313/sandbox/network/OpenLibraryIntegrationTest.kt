package com.prog7313.sandbox.network

import com.prog7313.sandbox.network.openlibrary.OpenLibraryApi
import com.prog7313.sandbox.network.openlibrary.OpenLibraryRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class OpenLibraryIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OpenLibraryApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
            .create(OpenLibraryApi::class.java)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun searchBooks_mockServerResponse_isDeserialisedAndReturned() =
        runBlocking {

            server.enqueue(
                MockResponse.Builder()
                    .code(200)
                    .body(
                        """
                        {
                          "numFound": 1,
                          "docs": [
                            {
                              "key": "/works/OL45804W",
                              "title": "Dune",
                              "author_name": ["Frank Herbert"],
                              "first_publish_year": 1965
                            }
                          ]
                        }
                        """.trimIndent()
                    )
                    .build()
            )

            val result =
                OpenLibraryRepository.searchBooks(
                    api = api,
                    query = "  Dune  "
                )

            assertEquals(
                1,
                result.numberFound
            )

            assertEquals(
                "Dune",
                result.docs.first().title
            )

            assertEquals(
                listOf("Frank Herbert"),
                result.docs.first().authorNames
            )

            assertEquals(
                1965,
                result.docs.first().firstPublishYear
            )

            val request =
                server.takeRequest()

            assertEquals(
                "/search.json",
                request.url.encodedPath
            )

            assertEquals(
                "Dune",
                request.url.queryParameter("q")
            )

            assertEquals(
                "5",
                request.url.queryParameter("limit")
            )

            assertTrue(
                request.url
                    .queryParameter("fields")
                    ?.contains("title") == true
            )
        }
}
