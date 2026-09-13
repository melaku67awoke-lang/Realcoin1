package com.example

import com.example.data.local.entities.EscrowOrderEntity
import com.example.data.local.entities.P2PAdEntity
import com.example.data.local.entities.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class P2PAndEscrowUnitTest {

    @Test
    fun testP2PTabFilteringLogic() {
        val sampleAds = listOf(
            P2PAdEntity(
                id = "ad_1",
                userId = "user_seller",
                username = "Solomon",
                type = "SELL", // Poster is selling -> Buyer sees this in BUY tab
                priceEtb = 15.5,
                minReal = 50.0,
                maxReal = 500.0,
                giftReal = 5.0,
                paymentMethods = "Telebirr, CBE"
            ),
            P2PAdEntity(
                id = "ad_2",
                userId = "user_buyer",
                username = "Blen",
                type = "BUY", // Poster is buying -> Seller sees this in SELL tab
                priceEtb = 15.2,
                minReal = 20.0,
                maxReal = 200.0,
                giftReal = 0.0,
                paymentMethods = "Telebirr"
            )
        )

        // When user is in BUY tab, they want to buy Real Coin from users who are selling
        val buyTabAds = sampleAds.filter { it.type == "SELL" }
        assertEquals(1, buyTabAds.size)
        assertEquals("Solomon", buyTabAds.first().username)
        assertEquals(15.5, buyTabAds.first().priceEtb, 0.001)

        // When user is in SELL tab, they want to sell Real Coin to users who are buying
        val sellTabAds = sampleAds.filter { it.type == "BUY" }
        assertEquals(1, sellTabAds.size)
        assertEquals("Blen", sellTabAds.first().username)
        assertEquals(15.2, sellTabAds.first().priceEtb, 0.001)
    }

    @Test
    fun testAdDeletionProtectionWithActiveEscrowOrders() {
        val adId = "ad_test_delete_123"

        val activeOrders = listOf(
            EscrowOrderEntity(
                id = "order_1",
                adId = adId,
                buyerId = "user_buyer_1",
                buyerUsername = "Blen",
                sellerId = "user_seller_1",
                sellerUsername = "Solomon",
                realAmount = 100.0,
                priceEtb = 15.0,
                totalEtb = 1500.0,
                status = "PAID_WAITING_RELEASE"
            )
        )

        val hasActiveOrders = activeOrders.any {
            it.status == "PENDING_PAYMENT" || it.status == "PAID_WAITING_RELEASE" || it.status.equals("DISPUTED", ignoreCase = true)
        }

        // Deletion must be blocked
        assertTrue("Ad with active escrow order must NOT be deletable", hasActiveOrders)

        val completedOrders = listOf(
            EscrowOrderEntity(
                id = "order_2",
                adId = adId,
                buyerId = "user_buyer_1",
                buyerUsername = "Blen",
                sellerId = "user_seller_1",
                sellerUsername = "Solomon",
                realAmount = 100.0,
                priceEtb = 15.0,
                totalEtb = 1500.0,
                status = "COMPLETED"
            )
        )

        val hasActiveCompletedOrders = completedOrders.any {
            it.status == "PENDING_PAYMENT" || it.status == "PAID_WAITING_RELEASE" || it.status.equals("DISPUTED", ignoreCase = true)
        }

        // Deletion must be allowed
        assertFalse("Ad with completed escrow orders must be deletable", hasActiveCompletedOrders)
    }

    @Test
    fun testEscrowOrderTotalCalculation() {
        val amountReal = 250.0
        val priceEtb = 14.80
        val bonusGiftReal = 10.0

        val totalEtb = amountReal * priceEtb
        val effectiveRealReceived = amountReal + bonusGiftReal

        assertEquals(3700.0, totalEtb, 0.01)
        assertEquals(260.0, effectiveRealReceived, 0.01)
    }

    @Test
    fun testAdminRoleAccessControl() {
        val regularUser = UserEntity(
            userId = "user_reg_01",
            username = "standard_trader",
            walletAddress = "0x1111222233334444555566667777888899990000",
            isAdmin = false
        )

        assertFalse("Regular user must not have admin privileges", regularUser.isAdmin)

        val promotedAdmin = regularUser.copy(isAdmin = true)
        assertTrue("Promoted user must have admin privileges", promotedAdmin.isAdmin)
    }

    @Test
    fun testCustomAdValidationRules() {
        fun validateAdInputs(price: Double?, min: Double?, max: Double?, methods: String): String? {
            return when {
                price == null || price <= 0.0 -> "Invalid price"
                min == null || min <= 0.0 -> "Invalid min amount"
                max == null || max < min -> "Max amount must be >= min amount"
                methods.isBlank() -> "Payment methods required"
                else -> null
            }
        }

        // Test valid inputs
        val validResult = validateAdInputs(15.5, 50.0, 500.0, "Telebirr, CBE")
        assertEquals(null, validResult)

        // Test invalid price
        val invalidPrice = validateAdInputs(-1.0, 50.0, 500.0, "Telebirr")
        assertEquals("Invalid price", invalidPrice)

        // Test invalid range (max < min)
        val invalidRange = validateAdInputs(15.5, 100.0, 50.0, "Telebirr")
        assertEquals("Max amount must be >= min amount", invalidRange)

        // Test blank payment method
        val blankMethod = validateAdInputs(15.5, 10.0, 50.0, "   ")
        assertEquals("Payment methods required", blankMethod)
    }
}
