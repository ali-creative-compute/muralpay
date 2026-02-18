package org.muralpay.project

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.muralpay.project.api.PaymentIntent
import org.muralpay.project.api.Product

object SeedData {
    fun initialize() {
        // Skip if already seeded
        if (KeyValueStore.exists(TEST_PRODUCT_ID)) {
            println("Database already seeded, skipping...")
            return
        }

        println("Seeding database with test data...")

        // Create test product
        val product = Product(
            id = TEST_PRODUCT_ID,
            name = "Test Widget",
            price = 15,
            currency = "USD"
        )
        KeyValueStore.put(TEST_PRODUCT_ID, Json.encodeToString(product))

        // Add product to merchant's product list
        val merchantProductsKey = "${TEST_MERCHANT_ID}#PRODUCTS"
        KeyValueStore.put(merchantProductsKey, Json.encodeToString(listOf(TEST_PRODUCT_ID)))

        // Create test payment intent
        val paymentIntent = PaymentIntent(
            id = TEST_PAYMENT_INTENT_ID,
            productIds = listOf(TEST_PRODUCT_ID),
            totalPrice = 15,
            currency = "USD"
        )
        KeyValueStore.put(TEST_PAYMENT_INTENT_ID, Json.encodeToString(paymentIntent))

        // Add payment intent to customer's intents list
        val customerIntentsKey = "${TEST_CUSTOMER_ID}#INTENTS"
        KeyValueStore.put(customerIntentsKey, Json.encodeToString(listOf(TEST_PAYMENT_INTENT_ID)))

        println("Database seeded successfully!")
        println("  Merchant ID: $TEST_MERCHANT_ID")
        println("  Product ID: $TEST_PRODUCT_ID")
        println("  Customer ID: $TEST_CUSTOMER_ID")
        println("  Payment Intent ID: $TEST_PAYMENT_INTENT_ID")
    }
}
