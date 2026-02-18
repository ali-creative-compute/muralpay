This is a Kotlin Multiplatform project targeting Server.

* [/server](./server/src/main/kotlin) is for the Ktor server application.

## Setup

This backend app is hosted via Railway. All setup is done and ready for cURL tests.

Railway URL: https://muralpay-production.up.railway.app

The following IDs and corresponding DB values are hydrated at startup to facilitate easier testing, but the product/checkout flows
can be used for dynamic/novel data.

> merchantId = merchant-test-001
> 
> productId = product-test-001
> 
> customerId = customer-test-001
> 
> paymentIntentId = pi_test-001 

## Current Status

#### Customer: Product Checkout & Payment Collection
1. Display a simple product catalog with items for sale

> (optional) Add products via /merchants/{merchantId}/products. See code/openAPI spec for payloads, else use example IDs provided above in following requests.
>
> GET /merchants/{merchantId}/products to list all products for a given merchant. e.g. use case is a customer is shopping on a given merchant's site.

2. Allow customers to shop for items

> POST /customers/{customerId}/payment_intents.
> 
> This starts the 'checkout' process, intention here is to create an Intent on setup of a Cart, and potentially update as needed. Later, this can be used to finally checkout and pay.

3. Allow customers to complete their checkout in the following currencies

> POST /customers/{customerId}/payment_intents/{paymentIntentId}
>
> With a returned paymentIntentId, initiate checkout. In the future, the /paymentMethods API would also be implemented to allow for dynamic payment methods and currencies.

**Full test of Payout** (uses pre-hydrated data, triggers full sandbox MuralPay payout):

```bash
curl -X POST https://muralpay-production.up.railway.app/customers/customer-test-001/payment_intents/pi_test-001 \
  -H "Content-Type: application/json" \
  -d '{}'
```

Expected response:
```json
{"payoutId":"130df139-6260-407c-86e2-8fec4f59f323","status":"PENDING"}
```

## Future Work

#### Merchant: Payment Receipt & Verification
1. Detect when a customer's payment has been received
   2. Subscription to the MURAL_ACCOUNT_BALANCE_ACTIVITY webhook and action accordingly.
      3. Manage Payment records in database. Update on customer payment initiation, and on webhooks.
2. Display payment status and confirmation to the merchant (customer?) for their orders
   3. As above, save and manage Payment lifecycle in database. 
   4. Simpler, forward status requests directly to https://api.muralpay.com/api/payouts/payout/{id} for this specific use case.
#### Merchant: Automatic Fund Conversion & Withdrawal
1. When payment is received, automatically initiate a conversion and transfer of funds to a bank account denominated in Colombian Pesos (COP)
   2. Similar to above, act on MURAL_ACCOUNT_BALANCE_ACTIVITY webhook. Initiate an Inline Fiat Payout to Colombian bank account.
2. A way for the Merchant to see the status of their withdrawals to COP
   3. Same as above, manage Payout status in internal Payment model. Else, forward status requests to https://api.muralpay.com/api/payouts/payout/{id}. 


---