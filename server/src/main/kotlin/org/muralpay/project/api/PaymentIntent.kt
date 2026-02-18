package org.muralpay.project.api

import kotlinx.serialization.Serializable

@Serializable
data class PaymentIntent(
    val id: String,
    val productIds: List<String>,
    val totalPrice: Int,
    val currency: String = "USD"
)

@Serializable
data class CreatePaymentIntentRequest(
    val productIds: List<String>
)