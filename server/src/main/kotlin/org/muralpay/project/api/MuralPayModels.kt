package org.muralpay.project.api

import kotlinx.serialization.Serializable

// Request models for creating a payout
@Serializable
data class CreatePayoutRequest(
    val sourceAccountId: String,
    val memo: String,
    val payouts: List<PayoutItem>
)

@Serializable
data class PayoutItem(
    val amount: PayoutAmount,
    val payoutDetails: PayoutDetails,
    val recipientInfo: RecipientInfo,
    val supportingDetails: SupportingDetails
)

@Serializable
data class PayoutAmount(
    val tokenAmount: Int,
    val tokenSymbol: String = "USDC"
)

@Serializable
data class PayoutDetails(
    val type: String = "blockchain",
    val walletDetails: WalletDetails
)

@Serializable
data class WalletDetails(
    val blockchain: String = "POLYGON",
    val walletAddress: String
)

@Serializable
data class RecipientInfo(
    val type: String = "business",
    val email: String,
    val physicalAddress: PhysicalAddress,
    val name: String
)

@Serializable
data class PhysicalAddress(
    val address1: String,
    val address2: String = "Suite 100",
    val country: String = "US",
    val state: String,
    val city: String,
    val zip: String
)

@Serializable
data class SupportingDetails(
    val supportingDocument: String = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    val payoutPurpose: String = "VENDOR_PAYMENT"
)

// Response model from create payout
@Serializable
data class PayoutResponse(
    val id: String,
    val status: String
)

// Request model for executing a payout
@Serializable
data class ExecutePayoutRequest(
    val exchangeRateToleranceMode: String = "FLEXIBLE"
)

// API response models
@Serializable
data class ExecutePaymentResponse(
    val payoutId: String,
    val status: String
)

@Serializable
data class CreatePaymentIntentResponse(
    val paymentIntentId: String
)

@Serializable
data class CreatePaymentMethodResponse(
    val paymentMethodId: String
)

@Serializable
data class ErrorResponse(
    val error: String
)
