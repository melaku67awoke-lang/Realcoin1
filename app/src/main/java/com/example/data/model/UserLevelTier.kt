package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class UserLevelTier(
    val levelName: String,
    val minDepositUsd: Double,
    val dailyRewardReal: Double,
    val multiplier: Double,
    val multiplierDisplay: String,
    val badgeColor: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val perks: List<String>
) {
    STARTER(
        levelName = "Starter",
        minDepositUsd = 50.0,
        dailyRewardReal = 15.0,
        multiplier = 1.0,
        multiplierDisplay = "1.0x",
        badgeColor = Color(0xFF38BDF8), // Sky blue
        gradientStart = Color(0xFF0284C7),
        gradientEnd = Color(0xFF075985),
        perks = listOf(
            "15 Real coin daily reward per day",
            "1.0x Base Rewards Multiplier",
            "Full P2P Escrow Trading Access",
            "Standard community chat and live broadcasting"
        )
    ),
    SILVER(
        levelName = "Silver",
        minDepositUsd = 75.0,
        dailyRewardReal = 30.0,
        multiplier = 2.0,
        multiplierDisplay = "2.0x",
        badgeColor = Color(0xFFE2E8F0), // Silver slate
        gradientStart = Color(0xFF94A3B8),
        gradientEnd = Color(0xFF475569),
        perks = listOf(
            "30 Real coin daily reward per day (2x)",
            "2.0x Rewards Multiplier",
            "Higher P2P buying and selling order limits",
            "Silver verified trader profile badge"
        )
    ),
    GOLD(
        levelName = "Gold",
        minDepositUsd = 100.0,
        dailyRewardReal = 50.0,
        multiplier = 3.33,
        multiplierDisplay = "3.33x",
        badgeColor = Color(0xFFFBBF24), // Gold amber
        gradientStart = Color(0xFFF59E0B),
        gradientEnd = Color(0xFF92400E),
        perks = listOf(
            "50 Real coin daily reward per day (3.33x)",
            "3.33x Rewards Multiplier",
            "Priority admin deposit verification queue",
            "Gold verified badge on P2P adverts"
        )
    ),
    PLATINUM(
        levelName = "Platinum",
        minDepositUsd = 125.0,
        dailyRewardReal = 75.0,
        multiplier = 5.0,
        multiplierDisplay = "5.0x",
        badgeColor = Color(0xFF22D3EE), // Cyan
        gradientStart = Color(0xFF06B6D4),
        gradientEnd = Color(0xFF155E75),
        perks = listOf(
            "75 Real coin daily reward per day (5x)",
            "5.0x Rewards Multiplier",
            "Expedited crypto withdrawals",
            "Platinum shield icon and live stream priority highlight"
        )
    ),
    DIAMOND(
        levelName = "Diamond",
        minDepositUsd = 150.0,
        dailyRewardReal = 100.0,
        multiplier = 6.67,
        multiplierDisplay = "6.67x",
        badgeColor = Color(0xFFA78BFA), // Purple violet
        gradientStart = Color(0xFF8B5CF6),
        gradientEnd = Color(0xFF5B21B6),
        perks = listOf(
            "100 Real coin daily reward per day (6.67x)",
            "6.67x Rewards Multiplier",
            "Zero P2P Escrow settlement fees",
            "Diamond crown avatar frame and VIP creator privileges"
        )
    ),
    VIP(
        levelName = "VIP",
        minDepositUsd = 200.0,
        dailyRewardReal = 150.0,
        multiplier = 10.0,
        multiplierDisplay = "10.0x",
        badgeColor = Color(0xFFF43F5E), // Rose red
        gradientStart = Color(0xFFE11D48),
        gradientEnd = Color(0xFF9F1239),
        perks = listOf(
            "150 Real coin daily reward per day (10x MAX)",
            "10.0x Maximum Rewards Multiplier",
            "Personal VIP account manager and 24/7 dedicated support",
            "Custom order sizes, zero limits and exclusive gifts"
        )
    );

    companion object {
        val ALL_TIERS = entries.toList()

        fun getTierForDeposit(totalUsdt: Double): UserLevelTier {
            return when {
                totalUsdt >= VIP.minDepositUsd -> VIP
                totalUsdt >= DIAMOND.minDepositUsd -> DIAMOND
                totalUsdt >= PLATINUM.minDepositUsd -> PLATINUM
                totalUsdt >= GOLD.minDepositUsd -> GOLD
                totalUsdt >= SILVER.minDepositUsd -> SILVER
                else -> STARTER
            }
        }

        fun getNextTier(currentTier: UserLevelTier): UserLevelTier? {
            val idx = entries.indexOf(currentTier)
            return if (idx < entries.size - 1) entries[idx + 1] else null
        }

        fun getDailyRewardForDeposit(totalUsdt: Double): Double {
            return if (totalUsdt >= STARTER.minDepositUsd) {
                getTierForDeposit(totalUsdt).dailyRewardReal
            } else {
                // If user has not yet reached $50 Starter deposit, base reward is 5 Real coin
                5.0
            }
        }

        fun calculateProgress(totalDepositUsdt: Double): UserLevelProgress {
            val isBelowStarter = totalDepositUsdt < STARTER.minDepositUsd
            val currentTier = getTierForDeposit(totalDepositUsdt)
            val nextTier = if (isBelowStarter) STARTER else getNextTier(currentTier)

            val currentTierBaseDeposit = if (isBelowStarter) 0.0 else currentTier.minDepositUsd
            val nextTierTargetDeposit = nextTier?.minDepositUsd ?: VIP.minDepositUsd

            val progressToNext: Float = if (nextTier == null) {
                1.0f // Max VIP reached
            } else {
                val span = nextTierTargetDeposit - currentTierBaseDeposit
                val currentInSpan = (totalDepositUsdt - currentTierBaseDeposit).coerceAtLeast(0.0)
                if (span > 0) (currentInSpan / span).toFloat().coerceIn(0f, 1f) else 1f
            }

            val amountNeeded = if (nextTier == null) {
                0.0
            } else {
                (nextTierTargetDeposit - totalDepositUsdt).coerceAtLeast(0.0)
            }

            val overallVipProgress = (totalDepositUsdt / VIP.minDepositUsd).toFloat().coerceIn(0f, 1f)

            val effectiveReward = getDailyRewardForDeposit(totalDepositUsdt)
            val effectiveMultiplier = if (isBelowStarter) 0.33 else currentTier.multiplier
            val effectiveMultiplierDisplay = if (isBelowStarter) "0.33x" else currentTier.multiplierDisplay

            return UserLevelProgress(
                currentTier = currentTier,
                nextTier = nextTier,
                isStarterUnlocked = !isBelowStarter,
                totalDepositUsd = totalDepositUsdt,
                currentTierBaseDeposit = currentTierBaseDeposit,
                nextTierTargetDeposit = nextTierTargetDeposit,
                progressToNextTier = progressToNext,
                amountNeededForNextTier = amountNeeded,
                overallVipProgress = overallVipProgress,
                dailyRewardReal = effectiveReward,
                rewardMultiplier = effectiveMultiplier,
                rewardMultiplierDisplay = effectiveMultiplierDisplay
            )
        }
    }
}

data class UserLevelProgress(
    val currentTier: UserLevelTier,
    val nextTier: UserLevelTier?,
    val isStarterUnlocked: Boolean,
    val totalDepositUsd: Double,
    val currentTierBaseDeposit: Double,
    val nextTierTargetDeposit: Double,
    val progressToNextTier: Float,
    val amountNeededForNextTier: Double,
    val overallVipProgress: Float,
    val dailyRewardReal: Double,
    val rewardMultiplier: Double,
    val rewardMultiplierDisplay: String
)
