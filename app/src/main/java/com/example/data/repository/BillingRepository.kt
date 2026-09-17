package com.example.data.repository

import com.example.data.local.UserPreferencesRepository
import com.example.data.model.Coupon
import com.example.data.model.SubscriptionRequest
import com.example.data.model.SubscriptionStatus
import com.example.data.remote.CouponRedemptionResult
import com.example.data.remote.FirestoreCouponService
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class StorePassItem(
    val productId: String,
    val title: String,
    val description: String,
    val priceString: String,
    val durationDays: Int,
    val isLifetime: Boolean = false,
    val isPopular: Boolean = false
)

class BillingRepository(
    private val preferencesRepository: UserPreferencesRepository,
    private val firestoreCouponService: FirestoreCouponService
) {

    val subscriptionRequestsFlow: StateFlow<List<SubscriptionRequest>> =
        firestoreCouponService.subscriptionRequestsFlow

    val availablePasses = listOf(
        StorePassItem(
            productId = "pass_1day",
            title = "1-Day Explorer Pass",
            description = "24 hours of full AI model access, image generation & all countries.",
            priceString = "$0.99",
            durationDays = 1
        ),
        StorePassItem(
            productId = "pass_7day",
            title = "7-Day Citizen Pass",
            description = "1 week of unlimited lives, business ownership & deep memory.",
            priceString = "$2.99",
            durationDays = 7,
            isPopular = true
        ),
        StorePassItem(
            productId = "pass_14day",
            title = "14-Day Sovereign Pass",
            description = "2 weeks of complete simulation freedom and 10 save slots.",
            priceString = "$4.99",
            durationDays = 14
        ),
        StorePassItem(
            productId = "pass_30day",
            title = "30-Day Master Pass",
            description = "1 month of elite access, PDF life story export & top priority quests.",
            priceString = "$8.99",
            durationDays = 30
        ),
        StorePassItem(
            productId = "lifetime_unlock",
            title = "Lifetime Universe Pass",
            description = "Permanent unlocked access to all features, present and future forever.",
            priceString = "$24.99",
            durationDays = -1,
            isLifetime = true
        )
    )

    /**
     * Submit a direct subscription request for Admin Review & Approval.
     * Google Pay dependency is bypassed; the Admin approves passes in the Admin Dashboard.
     */
    suspend fun requestSubscription(
        productId: String,
        userId: String,
        userName: String
    ): Result<SubscriptionRequest> {
        val pass = availablePasses.find { it.productId == productId }
            ?: return Result.failure(Exception("Product not found: $productId"))

        val request = SubscriptionRequest(
            id = "sub_req_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            userName = userName.ifBlank { "Citizen" },
            productId = pass.productId,
            productTitle = pass.title,
            priceString = pass.priceString,
            durationDays = pass.durationDays,
            requestTimestamp = System.currentTimeMillis(),
            status = SubscriptionStatus.PENDING
        )

        return firestoreCouponService.submitSubscriptionRequest(request)
    }

    /**
     * Admin approves a pending subscription request, granting entitlement to the player.
     */
    suspend fun approveSubscription(requestId: String, adminNotes: String? = null): Result<SubscriptionRequest> {
        val result = firestoreCouponService.updateSubscriptionStatus(
            requestId = requestId,
            status = SubscriptionStatus.APPROVED,
            adminNotes = adminNotes ?: "Approved by Administrator"
        )
        if (result.isSuccess) {
            val req = result.getOrThrow()
            preferencesRepository.setPassEntitlement(req.productId, req.durationDays)
        }
        return result
    }

    /**
     * Admin rejects a subscription request.
     */
    suspend fun rejectSubscription(requestId: String, reason: String? = null): Result<SubscriptionRequest> {
        return firestoreCouponService.updateSubscriptionStatus(
            requestId = requestId,
            status = SubscriptionStatus.REJECTED,
            adminNotes = reason ?: "Rejected by Administrator"
        )
    }

    suspend fun purchasePass(productId: String): Result<String> {
        val pass = availablePasses.find { it.productId == productId }
            ?: return Result.failure(Exception("Product not found"))

        preferencesRepository.setPassEntitlement(pass.productId, pass.durationDays)
        return Result.success("Successfully unlocked ${pass.title}!")
    }

    suspend fun redeemCoupon(code: String, userId: String): Result<CouponRedemptionResult> {
        val result = firestoreCouponService.validateAndRedeemCoupon(code, userId)
        if (result.isSuccess) {
            val redemption = result.getOrThrow()
            preferencesRepository.setPassEntitlement(
                redemption.grantedProductId,
                redemption.grantedDurationDays
            )
        }
        return result
    }

    suspend fun createAdminCoupon(coupon: Coupon): Result<Coupon> {
        return firestoreCouponService.createCoupon(coupon)
    }

    suspend fun getAllAdminCoupons(): List<Coupon> {
        return firestoreCouponService.getAllCoupons()
    }
}
