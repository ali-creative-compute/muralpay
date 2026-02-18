package org.muralpay.project.api

import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethod(
    val id: String,
    val currency: String
)

@Serializable
data class CreatePaymentMethodRequest(
    val currency: String
)
