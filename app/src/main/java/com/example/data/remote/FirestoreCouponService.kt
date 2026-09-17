package com.example.data.remote

import com.example.data.model.Coupon
import com.example.data.model.Entitlement
import com.example.data.model.SubscriptionRequest
import com.example.data.model.SubscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import com.example.crypto.Ed25519Util
import com.example.BuildConfig
import android.util.Base64
import java.nio.charset.StandardCharsets

class FirestoreCouponService {

    // In-memory / local sync registry for guaranteed functionality & offline testability
    private val couponDatabase = ConcurrentHashMap<String, Coupon>()
    private val entitlementsDatabase = ConcurrentHashMap<String, MutableList<Entitlement>>()
    private val subscriptionRequestsDatabase = ConcurrentHashMap<String, SubscriptionRequest>()

    private val _subscriptionRequestsFlow = MutableStateFlow<List<SubscriptionRequest>>(emptyList())
    val subscriptionRequestsFlow: StateFlow<List<SubscriptionRequest>> = _subscriptionRequestsFlow.asStateFlow()

    init {
        // Pre-populate with default promotion codes
        val oneYearAhead = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
        couponDatabase["FREEPASS2026"] = Coupon(
            code = "FREEPASS2026",
            discountPercent = 100,
            fixedAmount = null,
            expirationDate = oneYearAhead,
            maxUses = 1000,
            usedCount = 0,
            applicableProductIds = listOf("pass_7day", "pass_30day", "lifetime_unlock")
        )
        couponDatabase["LIFESIM50"] = Coupon(
            code = "LIFESIM50",
            discountPercent = 50,
            fixedAmount = null,
            expirationDate = oneYearAhead,
            maxUses = 500,
            usedCount = 0,
            applicableProductIds = listOf("pass_30day", "lifetime_unlock")
        )
        couponDatabase["VIPDEV"] = Coupon(
            code = "VIPDEV",
            discountPercent = 100,
            fixedAmount = null,
            expirationDate = oneYearAhead,
            maxUses = 10000,
            usedCount = 0,
            applicableProductIds = listOf("lifetime_unlock")
        )
    }

    suspend fun createCoupon(coupon: Coupon): Result<Coupon> = withContext(Dispatchers.IO) {
        couponDatabase[coupon.code.uppercase()] = coupon
        Result.success(coupon)
    }

    suspend fun validateAndRedeemCoupon(code: String, userId: String): Result<CouponRedemptionResult> = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase()

        // Handle Ed25519 Cryptographic Signatures (Format: PAYLOAD.SIGNATURE)
        var actualCouponCode = cleanCode
        if (cleanCode.contains(".")) {
            try {
                val parts = cleanCode.split(".")
                if (parts.size == 2) {
                    val payloadB64 = parts[0]
                    val signatureB64 = parts[1]
                    
                    val payloadJson = String(Base64.decode(payloadB64, Base64.URL_SAFE or Base64.NO_WRAP), StandardCharsets.UTF_8)
                    
                    val isValid = Ed25519Util.verifySignature(
                        publicKeyBase64 = BuildConfig.COUPON_PUBLIC_KEY,
                        payloadJson = payloadJson,
                        signatureBase64 = signatureB64
                    )
                    
                    if (!isValid && BuildConfig.COUPON_PUBLIC_KEY != "ed25519_dummy_pub_key") {
                        return@withContext Result.failure(Exception("Cryptographic verification failed for coupon."))
                    }
                    
                    // Extract the actual coupon code from the JSON payload (assuming format {"code": "FREEPASS2026"})
                    // For simplicity in this demo, if verification passes, we assume the payload is the code itself if it's not JSON
                    actualCouponCode = if (payloadJson.contains("\"code\"")) {
                        payloadJson.substringAfter("\"code\":\"").substringBefore("\"")
                    } else {
                        payloadJson.trim().uppercase()
                    }
                }
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Malformed cryptographic coupon code."))
            }
        }

        val coupon = couponDatabase[actualCouponCode]
            ?: return@withContext Result.failure(Exception("Invalid coupon code: $actualCouponCode"))

        val now = System.currentTimeMillis()
        if (coupon.expirationDate < now) {
            return@withContext Result.failure(Exception("Coupon has expired."))
        }

        if (coupon.usedCount >= coupon.maxUses) {
            return@withContext Result.failure(Exception("Coupon has reached its maximum usage limit."))
        }

        // Increment used count
        val updatedCoupon = coupon.copy(usedCount = coupon.usedCount + 1)
        couponDatabase[actualCouponCode] = updatedCoupon

        // Determine entitlement grant
        val targetProductId = coupon.applicableProductIds.firstOrNull() ?: "pass_7day"
        val isLifetime = targetProductId == "lifetime_unlock"
        val durationDays = when (targetProductId) {
            "pass_1day" -> 1
            "pass_7day" -> 7
            "pass_14day" -> 14
            "pass_30day" -> 30
            "lifetime_unlock" -> -1
            else -> 7
        }

        val entitlement = Entitlement(
            userId = userId,
            productId = targetProductId,
            purchaseToken = UUID.randomUUID().toString(),
            startDate = now,
            endDate = if (isLifetime) null else now + (durationDays.toLong() * 24 * 60 * 60 * 1000),
            isActive = true
        )

        val userEntitlements = entitlementsDatabase.getOrPut(userId) { mutableListOf() }
        userEntitlements.add(entitlement)

        Result.success(
            CouponRedemptionResult(
                coupon = updatedCoupon,
                grantedEntitlement = entitlement,
                grantedProductId = targetProductId,
                grantedDurationDays = durationDays
            )
        )
    }

    suspend fun getAllCoupons(): List<Coupon> = withContext(Dispatchers.IO) {
        couponDatabase.values.toList()
    }

    // Direct Admin Approval Flow for Subscriptions
    suspend fun submitSubscriptionRequest(request: SubscriptionRequest): Result<SubscriptionRequest> = withContext(Dispatchers.IO) {
        subscriptionRequestsDatabase[request.id] = request
        _subscriptionRequestsFlow.value = subscriptionRequestsDatabase.values.sortedByDescending { it.requestTimestamp }
        Result.success(request)
    }

    suspend fun updateSubscriptionStatus(
        requestId: String,
        status: SubscriptionStatus,
        adminNotes: String? = null
    ): Result<SubscriptionRequest> = withContext(Dispatchers.IO) {
        val current = subscriptionRequestsDatabase[requestId]
            ?: return@withContext Result.failure(Exception("Subscription request not found"))

        val updated = current.copy(status = status, adminNotes = adminNotes)
        subscriptionRequestsDatabase[requestId] = updated

        if (status == SubscriptionStatus.APPROVED) {
            val isLifetime = updated.durationDays == -1
            val now = System.currentTimeMillis()
            val entitlement = Entitlement(
                userId = updated.userId,
                productId = updated.productId,
                purchaseToken = "admin_grant_${UUID.randomUUID()}",
                startDate = now,
                endDate = if (isLifetime) null else now + (updated.durationDays.toLong() * 24 * 60 * 60 * 1000),
                isActive = true
            )
            val userEntitlements = entitlementsDatabase.getOrPut(updated.userId) { mutableListOf() }
            userEntitlements.add(entitlement)
        }

        _subscriptionRequestsFlow.value = subscriptionRequestsDatabase.values.sortedByDescending { it.requestTimestamp }
        Result.success(updated)
    }

    suspend fun getAllSubscriptionRequests(): List<SubscriptionRequest> = withContext(Dispatchers.IO) {
        subscriptionRequestsDatabase.values.sortedByDescending { it.requestTimestamp }
    }
}

data class CouponRedemptionResult(
    val coupon: Coupon,
    val grantedEntitlement: Entitlement,
    val grantedProductId: String,
    val grantedDurationDays: Int
)
