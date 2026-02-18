package org.muralpay.project

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.muralpay.project.api.*
import java.util.UUID

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: SERVER_PORT
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    // Seed database with test data on startup
    SeedData.initialize()

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        post("/merchants/{merchantId}/products") {
            val merchantId = call.parameters["merchantId"]!!
            val product = call.receive<Product>()

            // Store product: key = productId, value = product JSON
            val productJson = Json.encodeToString(product)
            KeyValueStore.put(product.id, productJson)

            // Update merchant's product list: key = {merchantId}#PRODUCTS
            val merchantProductsKey = "${merchantId}#PRODUCTS"
            val existingProductIds = KeyValueStore.get(merchantProductsKey)
                ?.let { Json.decodeFromString<List<String>>(it) }
                ?: emptyList()

            val updatedProductIds = existingProductIds + product.id
            KeyValueStore.put(merchantProductsKey, Json.encodeToString(updatedProductIds))

            call.respond(HttpStatusCode.Created, product)
        }

        get("/merchants/{merchantId}/products") {
            val merchantId = call.parameters["merchantId"]!!
            val merchantProductsKey = "${merchantId}#PRODUCTS"

            val productIds = KeyValueStore.get(merchantProductsKey)
                ?.let { Json.decodeFromString<List<String>>(it) }
                ?: emptyList()

            val products = productIds.mapNotNull { productId ->
                KeyValueStore.get(productId)?.let { Json.decodeFromString<Product>(it) }
            }

            call.respond(products)
        }

        post("/customers/{customerId}/payment_intents") {
            val customerId = call.parameters["customerId"]!!
            val request = call.receive<CreatePaymentIntentRequest>()

            // Retrieve products and sum prices
            val products = request.productIds.mapNotNull { productId ->
                KeyValueStore.get(productId)?.let { Json.decodeFromString<Product>(it) }
            }
            val totalPrice = products.sumOf { it.price }

            // Generate payment intent ID and create PaymentIntent
            val paymentIntentId = "pi_${UUID.randomUUID()}"
            val paymentIntent = PaymentIntent(
                id = paymentIntentId,
                productIds = request.productIds,
                totalPrice = totalPrice
            )

            // Store PaymentIntent: key = paymentIntentId, value = PaymentIntent JSON
            KeyValueStore.put(paymentIntentId, Json.encodeToString(paymentIntent))

            // Update customer's intents list: key = {customerId}#INTENTS
            val customerIntentsKey = "${customerId}#INTENTS"
            val existingIntentIds = KeyValueStore.get(customerIntentsKey)
                ?.let { Json.decodeFromString<List<String>>(it) }
                ?: emptyList()

            val updatedIntentIds = existingIntentIds + paymentIntentId
            KeyValueStore.put(customerIntentsKey, Json.encodeToString(updatedIntentIds))

            call.respond(HttpStatusCode.Created, CreatePaymentIntentResponse(paymentIntentId))
        }

        post("/customers/{customerId}/payment_methods") {
            // TODO
        }

        post("/customers/{customerId}/payment_intents/{paymentIntentId}") {
            val customerId = call.parameters["customerId"]!!
            val paymentIntentId = call.parameters["paymentIntentId"]!!

            // Get PaymentIntent from store
            val paymentIntentJson = KeyValueStore.get(paymentIntentId)
            if (paymentIntentJson == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("PaymentIntent not found"))
                return@post
            }
            val paymentIntent = Json.decodeFromString<PaymentIntent>(paymentIntentJson)

            // Create payout via MuralPay API
            val createResponse = MuralPayClient.createPayout(
                totalAmount = paymentIntent.totalPrice,
                recipientWallet = BIZ_WALLET
            )

            // Execute payout
            val executeResponse = MuralPayClient.executePayout(createResponse.id)

            // Store payout record: key = {paymentIntentId}#PAYOUT
            val payoutRecord = ExecutePaymentResponse(
                payoutId = executeResponse.id,
                status = executeResponse.status
            )
            KeyValueStore.put("${paymentIntentId}#PAYOUT", Json.encodeToString(payoutRecord))

            call.respond(HttpStatusCode.OK, payoutRecord)
        }

    }
}