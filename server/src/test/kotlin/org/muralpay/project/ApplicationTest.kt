package org.muralpay.project

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class ApplicationTest {

    @Test
    fun `root endpoint returns greeting`() = testApplication {
        application {
            module()
        }

        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Ktor:")
    }

    @Test
    fun `root endpoint responds with correct content type`() = testApplication {
        application {
            module()
        }

        val response = client.get("/")

        assertEquals(ContentType.Text.Plain, response.contentType()?.withoutParameters())
    }
}
