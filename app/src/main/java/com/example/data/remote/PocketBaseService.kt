package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class ValidateReceiptRequest(
    @Json(name = "productId") val productId: String,
    @Json(name = "purchaseToken") val purchaseToken: String
)

@JsonClass(generateAdapter = true)
data class ValidateReceiptResponse(
    @Json(name = "valid") val valid: Boolean,
    @Json(name = "productId") val productId: String?,
    @Json(name = "expiry") val expiry: Long?,
    @Json(name = "entitlementId") val entitlementId: String?
)

@JsonClass(generateAdapter = true)
data class RedeemCouponRequest(
    @Json(name = "code") val code: String
)

@JsonClass(generateAdapter = true)
data class RedeemCouponResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "code") val code: String,
    @Json(name = "discountPercent") val discountPercent: Int?,
    @Json(name = "grantedFreePass") val grantedFreePass: Boolean?
)

@JsonClass(generateAdapter = true)
data class GenerateCouponRequest(
    @Json(name = "code") val code: String,
    @Json(name = "discountPercent") val discountPercent: Int,
    @Json(name = "maxUses") val maxUses: Int,
    @Json(name = "expiryDays") val expiryDays: Int
)

interface PocketBaseService {

    @POST("/api/validate-receipt")
    suspend fun validateReceipt(
        @Header("Authorization") token: String,
        @Body request: ValidateReceiptRequest
    ): Response<ValidateReceiptResponse>

    @POST("/api/redeem-coupon")
    suspend fun redeemCoupon(
        @Header("Authorization") token: String,
        @Body request: RedeemCouponRequest
    ): Response<RedeemCouponResponse>

    @POST("/api/admin/generate-coupon")
    suspend fun generateCoupon(
        @Header("X-Admin-Secret") adminSecret: String,
        @Body request: GenerateCouponRequest
    ): Response<Map<String, Any>>
}
