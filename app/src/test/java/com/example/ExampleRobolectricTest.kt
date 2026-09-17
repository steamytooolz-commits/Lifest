package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.SubscriptionStatus
import com.example.data.remote.FirestoreCouponService
import com.example.data.repository.BillingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Life Simulator", appName)
  }

  @Test
  fun `admin manual subscription approval grants pass correctly`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefsRepo = UserPreferencesRepository(context)
    val firestoreService = FirestoreCouponService()
    val billingRepo = BillingRepository(prefsRepo, firestoreService)

    // 1. Submit subscription request for Admin Approval (No Google Pay)
    val requestResult = billingRepo.requestSubscription(
      productId = "pass_30day",
      userId = "citizen_test_123",
      userName = "Test Player"
    )
    assertTrue("Request should succeed", requestResult.isSuccess)
    val request = requestResult.getOrThrow()
    assertEquals(SubscriptionStatus.PENDING, request.status)

    // Verify queue
    val requests = billingRepo.subscriptionRequestsFlow.first()
    assertEquals(1, requests.size)
    assertEquals("pass_30day", requests[0].productId)

    // 2. Admin approves subscription
    val approvalResult = billingRepo.approveSubscription(request.id, "Manually verified by Admin")
    assertTrue("Approval should succeed", approvalResult.isSuccess)
    val approvedReq = approvalResult.getOrThrow()
    assertEquals(SubscriptionStatus.APPROVED, approvedReq.status)

    // 3. User entitlement check
    val prefs = prefsRepo.userPreferencesFlow.first()
    assertTrue("User should now be paid", prefs.isPaidUser)
    assertEquals("pass_30day", prefs.activePassType)
    assertTrue("Pass expiration must be greater than 0", prefs.passExpirationEpochMs > 0L)
  }
}
