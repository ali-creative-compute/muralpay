package org.muralpay.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.muralpay.project.api.*

object MuralPayClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    suspend fun createPayout(totalAmount: Int, recipientWallet: String): PayoutResponse {
        val request = CreatePayoutRequest(
            sourceAccountId = MURAL_SOURCE_ACCOUNT_ID,
            memo = "Payment via MuralPay",
            payouts = listOf(
                PayoutItem(
                    amount = PayoutAmount(tokenAmount = totalAmount),
                    payoutDetails = PayoutDetails(
                        walletDetails = WalletDetails(walletAddress = recipientWallet)
                    ),
                    recipientInfo = RecipientInfo(
                        email = "payment@example.com",
                        physicalAddress = PhysicalAddress(
                            address1 = "123 Main St",
                            state = "IL",
                            city = "Chicago",
                            zip = "60601"
                        ),
                        name = "Recipient Business"
                    ),
                    supportingDetails = SupportingDetails()
                )
            )
        )

        val response = client.post("$MURAL_API_BASE_URL/payouts/payout") {
            contentType(ContentType.Application.Json)
            header("accept", "application/json")
            header("authorization", "Bearer $MURAL_API_KEY")
            header("on-behalf-of", MURAL_ON_BEHALF_OF)
            setBody(request)
        }

        val bodyText = response.bodyAsText()
        println("Create Payout Response [${response.status}]: $bodyText")

        if (!response.status.isSuccess()) {
            throw RuntimeException("Create payout failed: $bodyText")
        }

        return json.decodeFromString<PayoutResponse>(bodyText)
    }

    suspend fun executePayout(payoutId: String): PayoutResponse {
        val request = ExecutePayoutRequest()

        val response = client.post("$MURAL_API_BASE_URL/payouts/payout/$payoutId/execute") {
            contentType(ContentType.Application.Json)
            header("accept", "application/json")
            header("authorization", "Bearer $MURAL_API_KEY")
            header("on-behalf-of", MURAL_ON_BEHALF_OF)
            header("transfer-api-key", MURAL_TRANSFER_API_KEY)
            setBody(request)
        }

        val bodyText = response.bodyAsText()
        println("Execute Payout Response [${response.status}]: $bodyText")

        if (!response.status.isSuccess()) {
            throw RuntimeException("Execute payout failed: $bodyText")
        }

        return json.decodeFromString<PayoutResponse>(bodyText)
    }
}
