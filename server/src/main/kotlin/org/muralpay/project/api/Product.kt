package org.muralpay.project.api

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Int, // lowest denom (cents in USD)
    val currency: String = "USD")