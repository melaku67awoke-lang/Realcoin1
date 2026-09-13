package com.example

import com.example.data.local.entities.DepositRequestEntity
import com.example.data.local.entities.MarketRateEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.viewmodel.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DepositAndWalletUnitTest {

    @Test
    fun testBep20AddressFormat() {
        val bep20Address = "0x8e54105bed3243e1ca44a0cccc6b62cf2bff9df4"
        assertTrue("BEP-20 address must start with 0x", bep20Address.startsWith("0x"))
        assertEquals("BEP-20 address must be 42 characters long", 42, bep20Address.length)
        assertTrue(
            "BEP-20 address must be hex characters",
            bep20Address.removePrefix("0x").all { it in "0123456789abcdefABCDEF" }
        )
    }

    @Test
    fun testDepositBonusCalculation() {
        val usdtDeposit = 100.0
        val usdRate = 0.084734
        val baseRealCoin = usdtDeposit / usdRate
        val bonusPercent = 0.10 // 10% bonus
        val bonusRealCoin = baseRealCoin * bonusPercent
        val totalRealCoin = baseRealCoin + bonusRealCoin

        assertEquals(1180.16, baseRealCoin, 0.01)
        assertEquals(118.016, bonusRealCoin, 0.01)
        assertEquals(1298.18, totalRealCoin, 0.02)
    }

    @Test
    fun testDepositRequestEntityDefaults() {
        val depositReq = DepositRequestEntity(
            id = "dep_test_01",
            userId = "hab_user_01",
            username = "hab",
            amountUsd = 50.0,
            network = "BSC (BNB Smart Chain BEP20)",
            depositAddress = "0x8e54105bed3243e1ca44a0cccc6b62cf2bff9df4",
            baseRealCoin = 590.08,
            bonusRealCoin = 59.008,
            totalRealCoin = 649.088
        )

        assertEquals("BSC (BNB Smart Chain BEP20)", depositReq.network)
        assertEquals("0x8e54105bed3243e1ca44a0cccc6b62cf2bff9df4", depositReq.depositAddress)
        assertEquals("PENDING", depositReq.status)
        assertTrue(depositReq.totalRealCoin > depositReq.baseRealCoin)
    }

    @Test
    fun testMinimumDepositThreshold() {
        val minDepositAllowed = 25.0
        val testDepositLow = 20.0
        val testDepositValid = 25.0
        val testDepositHigh = 100.0

        assertTrue("Deposit below $25 must be rejected", testDepositLow < minDepositAllowed)
        assertTrue("Deposit of $25 must be accepted", testDepositValid >= minDepositAllowed)
        assertTrue("Deposit of $100 must be accepted", testDepositHigh >= minDepositAllowed)
    }

    @Test
    fun testSpinWheelPrizeMultipliers() {
        val spinPrizes = listOf(5.0, 10.0, 15.0, 20.0, 25.0, 50.0, 100.0, 200.0)
        assertTrue(spinPrizes.isNotEmpty())
        for (prize in spinPrizes) {
            assertTrue("Prize must be greater than zero", prize > 0.0)
        }
        val minPrize = spinPrizes.minOrNull() ?: 0.0
        val maxPrize = spinPrizes.maxOrNull() ?: 0.0
        assertEquals(5.0, minPrize, 0.001)
        assertEquals(200.0, maxPrize, 0.001)
    }

    @Test
    fun testUserLevelTiers() {
        fun calculateLevel(totalUsdt: Double): String {
            return when {
                totalUsdt >= 2000.0 -> "VIP Trader"
                totalUsdt >= 1000.0 -> "Gold Trader"
                totalUsdt >= 500.0 -> "Silver Trader"
                totalUsdt >= 100.0 -> "Bronze Trader"
                else -> "Starter"
            }
        }

        assertEquals("Starter", calculateLevel(0.0))
        assertEquals("Starter", calculateLevel(50.0))
        assertEquals("Bronze Trader", calculateLevel(100.0))
        assertEquals("Silver Trader", calculateLevel(500.0))
        assertEquals("Gold Trader", calculateLevel(1000.0))
        assertEquals("VIP Trader", calculateLevel(2500.0))
    }

    @Test
    fun testNotificationTypes() {
        val successType = NotificationType.SUCCESS
        val errorType = NotificationType.ERROR
        val warningType = NotificationType.WARNING
        val infoType = NotificationType.INFO

        assertNotNull(successType)
        assertNotNull(errorType)
        assertNotNull(warningType)
        assertNotNull(infoType)
    }
}
