package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.CryptoCardBg
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardVariant
import com.example.ui.theme.CryptoDarkBg
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.RealGoldLight
import com.example.ui.theme.RealGoldPrimary
import com.example.ui.theme.RealGoldSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class KycDocType(val displayName: String, val icon: ImageVector, val placeholder: String) {
    NATIONAL_ID("National ID / Kebele Card", Icons.Default.Badge, "ETH-99428-ID"),
    PASSPORT("Ethiopian Passport", Icons.Default.CreditCard, "EP-9823412"),
    DRIVERS_LICENSE("Driver's License", Icons.Default.Security, "DL-0482910"),
    RESIDENCE_PERMIT("Residence Permit", Icons.Default.VerifiedUser, "RP-110293")
}

@Composable
fun KYCScreen(
    user: UserEntity?,
    onBackClick: () -> Unit,
    onSubmitKyc: (docNum: String, frontUri: String?, backUri: String?, status: String) -> Unit,
    onStatusChange: ((String) -> Unit)? = null
) {
    val currentStatus = user?.kycStatus ?: "APPROVED"
    
    // Step State: 0 = Status & Overview, 1 = Personal Info, 2 = Document Selection, 3 = Document Uploads, 4 = Review & Submit
    var currentStep by remember { mutableIntStateOf(0) }
    
    // Step 1: Personal Info
    var fullName by remember { mutableStateOf(user?.fullName ?: "Habtamu K.") }
    var dobDay by remember { mutableStateOf("14") }
    var dobMonth by remember { mutableStateOf("08") }
    var dobYear by remember { mutableStateOf("1996") }
    var nationality by remember { mutableStateOf("Ethiopia") }
    var city by remember { mutableStateOf("Addis Ababa") }
    var subCity by remember { mutableStateOf("Bole Sub-City, Woreda 03") }
    
    // Step 2: Document Selection
    var selectedDocType by remember { mutableStateOf(KycDocType.NATIONAL_ID) }
    var idNumber by remember { mutableStateOf(user?.kycDocumentNumber ?: "ETH-99428-ID") }
    var issuingAuthority by remember { mutableStateOf("National ID Program (NIDP)") }
    var expiryDate by remember { mutableStateOf("12/2030") }

    // Step 3: Document Uploads
    var frontUploaded by remember { mutableStateOf(user?.kycIdFrontUri != null || currentStatus == "APPROVED") }
    var backUploaded by remember { mutableStateOf(user?.kycIdBackUri != null || currentStatus == "APPROVED") }
    var selfieUploaded by remember { mutableStateOf(currentStatus == "APPROVED") }
    
    var frontFileName by remember { mutableStateOf("national_id_front.jpg") }
    var backFileName by remember { mutableStateOf("national_id_back.jpg") }
    var selfieFileName by remember { mutableStateOf("selfie_verification.jpg") }

    // Step 4: Confirmations
    var termsAgreed by remember { mutableStateOf(true) }
    var infoAccurateAgreed by remember { mutableStateOf(true) }
    var autoApproveDemo by remember { mutableStateOf(false) }

    // Submission success confirmation dialog
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedRefCode by remember { mutableStateOf("KYC-2026-ETH99428") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CryptoDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- TOP APP BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (currentStep > 0) {
                    currentStep--
                } else {
                    onBackClick()
                }
            }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (currentStep == 0) "Identity Verification (KYC)" else "KYC Verification - Step $currentStep of 4",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (currentStep == 0) "Verification status & document management" else getStepSubtitle(currentStep),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- STEPPER PROGRESS BAR (when in flow 1..4) ---
        if (currentStep > 0) {
            KycStepIndicator(
                currentStep = currentStep,
                onStepSelect = { step ->
                    // Only allow navigating backwards or within reached steps
                    if (step <= currentStep) currentStep = step
                }
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // --- VIEW ROUTING BASED ON STEP ---
        when (currentStep) {
            0 -> {
                // STATUS OVERVIEW SCREEN
                KycStatusOverview(
                    status = currentStatus,
                    docNumber = user?.kycDocumentNumber ?: idNumber,
                    userName = user?.fullName ?: fullName,
                    onStartVerification = { currentStep = 1 },
                    onStatusChange = onStatusChange
                )
            }
            1 -> {
                // STEP 1: PERSONAL INFORMATION
                KycStepPersonalInfo(
                    fullName = fullName,
                    onFullNameChange = { fullName = it },
                    dobDay = dobDay,
                    onDobDayChange = { dobDay = it },
                    dobMonth = dobMonth,
                    onDobMonthChange = { dobMonth = it },
                    dobYear = dobYear,
                    onDobYearChange = { dobYear = it },
                    nationality = nationality,
                    onNationalityChange = { nationality = it },
                    city = city,
                    onCityChange = { city = it },
                    subCity = subCity,
                    onSubCityChange = { subCity = it },
                    email = user?.email ?: "melaku61awoke@gmail.com",
                    phone = user?.phone ?: "+251 998 856 991",
                    onNext = { currentStep = 2 }
                )
            }
            2 -> {
                // STEP 2: DOCUMENT SELECTION & NUMBER
                KycStepDocumentSelect(
                    selectedType = selectedDocType,
                    onTypeSelect = { selectedDocType = it },
                    idNumber = idNumber,
                    onIdNumberChange = { idNumber = it },
                    issuingAuthority = issuingAuthority,
                    onIssuingAuthorityChange = { issuingAuthority = it },
                    expiryDate = expiryDate,
                    onExpiryDateChange = { expiryDate = it },
                    onPrevious = { currentStep = 1 },
                    onNext = { currentStep = 3 }
                )
            }
            3 -> {
                // STEP 3: DOCUMENT UPLOADS & SELFIE
                KycStepDocumentUploads(
                    selectedType = selectedDocType,
                    frontUploaded = frontUploaded,
                    onFrontUpload = { frontUploaded = true },
                    onFrontRemove = { frontUploaded = false },
                    frontFileName = frontFileName,
                    backUploaded = backUploaded,
                    onBackUpload = { backUploaded = true },
                    onBackRemove = { backUploaded = false },
                    backFileName = backFileName,
                    selfieUploaded = selfieUploaded,
                    onSelfieUpload = { selfieUploaded = true },
                    onSelfieRemove = { selfieUploaded = false },
                    selfieFileName = selfieFileName,
                    onPrevious = { currentStep = 2 },
                    onNext = { currentStep = 4 }
                )
            }
            4 -> {
                // STEP 4: REVIEW & SUBMISSION
                KycStepReviewAndSubmit(
                    fullName = fullName,
                    dob = "$dobDay/$dobMonth/$dobYear",
                    nationality = nationality,
                    city = "$city, $subCity",
                    docType = selectedDocType.displayName,
                    docNumber = idNumber,
                    frontUploaded = frontUploaded,
                    backUploaded = backUploaded,
                    selfieUploaded = selfieUploaded,
                    termsAgreed = termsAgreed,
                    onTermsAgreedChange = { termsAgreed = it },
                    infoAccurateAgreed = infoAccurateAgreed,
                    onInfoAccurateChange = { infoAccurateAgreed = it },
                    autoApproveDemo = autoApproveDemo,
                    onAutoApproveDemoChange = { autoApproveDemo = it },
                    onPrevious = { currentStep = 3 },
                    onSubmit = {
                        val targetStatus = if (autoApproveDemo) "APPROVED" else "PENDING"
                        onSubmitKyc(
                            idNumber,
                            if (frontUploaded) "uri://kyc_front_$frontFileName" else null,
                            if (backUploaded) "uri://kyc_back_$backFileName" else null,
                            targetStatus
                        )
                        submittedRefCode = "KYC-2026-" + idNumber.takeLast(6).replace("-", "")
                        showSuccessDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Footnote
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "End-to-end encrypted with 256-bit AES standards. Complies with Ethiopian AML & National Bank regulations.",
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }

    // --- SUBMISSION SUCCESS MODAL ---
    if (showSuccessDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {
            showSuccessDialog = false
            currentStep = 0
        }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (autoApproveDemo) CryptoGreen.copy(alpha = 0.15f) else RealGoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (autoApproveDemo) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = "Success",
                            tint = if (autoApproveDemo) CryptoGreen else RealGoldPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (autoApproveDemo) "KYC Verified Instantly!" else "Documents Submitted!",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (autoApproveDemo)
                            "Congratulations! Your identity documents have been approved. You now have Tier 2 verification status with unlimited P2P privileges."
                        else
                            "Your identity documents and selfie have been securely received. Our compliance team audits submissions within 1 to 24 hours.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tracking code card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CryptoCardVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "REFERENCE TRACKING ID", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = submittedRefCode, color = RealGoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            currentStep = 0
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RealGoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(text = "View Verification Status", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun getStepSubtitle(step: Int): String = when (step) {
    1 -> "Step 1: Personal & Legal Details"
    2 -> "Step 2: Document Selection & Identification"
    3 -> "Step 3: Document Photos & Selfie Liveness Check"
    4 -> "Step 4: Review & Final Submission"
    else -> ""
}

// =========================================================================
// STEPPER PROGRESS COMPONENT
// =========================================================================
@Composable
fun KycStepIndicator(
    currentStep: Int,
    onStepSelect: (Int) -> Unit
) {
    val steps = listOf("Personal", "Document", "Uploads", "Review")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, stepLabel ->
                val stepNum = index + 1
                val isDone = stepNum < currentStep
                val isCurrent = stepNum == currentStep

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onStepSelect(stepNum) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> CryptoGreen
                                    isCurrent -> RealGoldPrimary
                                    else -> CryptoCardVariant
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = when {
                                    isDone -> CryptoGreen
                                    isCurrent -> RealGoldSecondary
                                    else -> CryptoCardBorder
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "$stepNum",
                                color = if (isCurrent) Color.Black else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stepLabel,
                        color = if (isCurrent) RealGoldPrimary else if (isDone) TextPrimary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentStep - 1) / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = RealGoldPrimary,
            trackColor = CryptoCardBorder
        )
    }
}

// =========================================================================
// SCREEN 0: STATUS OVERVIEW
// =========================================================================
@Composable
fun KycStatusOverview(
    status: String,
    docNumber: String,
    userName: String,
    onStartVerification: () -> Unit,
    onStatusChange: ((String) -> Unit)?
) {
    val isApproved = status == "APPROVED"
    val isPending = status == "PENDING"
    val isRejected = status == "REJECTED"
    val isNotSubmitted = status == "NOT_SUBMITTED" || (!isApproved && !isPending && !isRejected)

    // Current Status Banner Card
    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isApproved -> CryptoGreen.copy(alpha = 0.15f)
                                    isPending -> RealGoldPrimary.copy(alpha = 0.15f)
                                    isRejected -> CryptoRed.copy(alpha = 0.15f)
                                    else -> TextMuted.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isApproved -> Icons.Default.CheckCircle
                                isPending -> Icons.Default.Schedule
                                isRejected -> Icons.Default.ErrorOutline
                                else -> Icons.Default.Security
                            },
                            contentDescription = "Status Icon",
                            tint = when {
                                isApproved -> CryptoGreen
                                isPending -> RealGoldPrimary
                                isRejected -> CryptoRed
                                else -> TextSecondary
                            },
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = when {
                                isApproved -> "Identity Verified"
                                isPending -> "Verification In Review"
                                isRejected -> "Resubmission Required"
                                else -> "Not Verified"
                            },
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when {
                                isApproved -> "Tier 2 Level Verified Account"
                                isPending -> "Audit in progress (~1-24h)"
                                isRejected -> "Documents need updating"
                                else -> "Unlock high limits & P2P trading"
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isApproved -> CryptoGreen.copy(alpha = 0.15f)
                                isPending -> RealGoldPrimary.copy(alpha = 0.15f)
                                isRejected -> CryptoRed.copy(alpha = 0.15f)
                                else -> TextMuted.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = when {
                            isApproved -> "VERIFIED"
                            isPending -> "PENDING"
                            isRejected -> "REJECTED"
                            else -> "UNVERIFIED"
                        },
                        color = when {
                            isApproved -> CryptoGreen
                            isPending -> RealGoldPrimary
                            isRejected -> CryptoRed
                            else -> TextSecondary
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details based on status
            when {
                isApproved -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CryptoCardVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "VERIFIED HOLDER", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = userName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "DOCUMENT NUMBER", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = docNumber, color = RealGoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                isPending -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(RealGoldPrimary.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = RealGoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Your documents are currently being inspected by compliance officers.",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Reference: KYC-2026-ETH99428 • Submitted today",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                isRejected -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CryptoRed.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = CryptoRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reason: Document back photo was blurry or had glare.",
                                color = CryptoRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please submit a clear, well-lit photo of both sides of your ID card.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Complete your identity verification in 4 quick steps to start buying, selling, and withdrawing funds on Real Coin.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Action Button
            Button(
                onClick = onStartVerification,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RealGoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_kyc_verification_button")
            ) {
                Icon(
                    imageVector = if (isApproved) Icons.Default.Refresh else Icons.Default.VerifiedUser,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isApproved -> "Update / Re-verify Documents"
                        isPending -> "Edit / Re-submit Information"
                        isRejected -> "Resubmit KYC Documents"
                        else -> "Start Identity Verification"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // --- VERIFICATION TIERS BREAKDOWN ---
    Text(
        text = "Account Verification Tiers",
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(10.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TierRow(
                tierName = "Tier 1: Basic",
                statusLabel = "Completed",
                limit = "10,000 ETB / day",
                features = "Wallet deposits, daily spin rewards",
                isAchieved = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.HorizontalDivider(color = CryptoCardBorder)
            Spacer(modifier = Modifier.height(12.dp))
            TierRow(
                tierName = "Tier 2: Government ID",
                statusLabel = if (isApproved) "Active" else "Target",
                limit = "2,000,000 ETB / day",
                features = "P2P merchant privileges, zero escrow hold, fast fiat payouts",
                isAchieved = isApproved,
                highlight = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.HorizontalDivider(color = CryptoCardBorder)
            Spacer(modifier = Modifier.height(12.dp))
            TierRow(
                tierName = "Tier 3: Institutional",
                statusLabel = "Enterprise",
                limit = "Unlimited ETB",
                features = "Dedicated account manager & API access",
                isAchieved = false
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // --- DEMO / TESTING QUICK STATUS TOGGLE ---
    if (onStatusChange != null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoCardVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Demo Testing: Fast Status Switcher",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("NOT_SUBMITTED", "PENDING", "APPROVED", "REJECTED").forEach { s ->
                        val isSelected = status == s
                        OutlinedButton(
                            onClick = { onStatusChange(s) },
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) RealGoldPrimary else Color.Transparent,
                                contentColor = if (isSelected) Color.Black else TextSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(if (isSelected) RealGoldPrimary else CryptoCardBorder)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = when (s) {
                                    "NOT_SUBMITTED" -> "New"
                                    "PENDING" -> "Review"
                                    "APPROVED" -> "Verify"
                                    "REJECTED" -> "Reject"
                                    else -> s
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TierRow(
    tierName: String,
    statusLabel: String,
    limit: String,
    features: String,
    isAchieved: Boolean,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tierName,
                    color = if (highlight) RealGoldPrimary else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isAchieved) CryptoGreen.copy(alpha = 0.15f) else CryptoCardVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = if (isAchieved) CryptoGreen else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Daily Limit: $limit", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = features, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// =========================================================================
// STEP 1: PERSONAL INFORMATION
// =========================================================================
@Composable
fun KycStepPersonalInfo(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    dobDay: String,
    onDobDayChange: (String) -> Unit,
    dobMonth: String,
    onDobMonthChange: (String) -> Unit,
    dobYear: String,
    onDobYearChange: (String) -> Unit,
    nationality: String,
    onNationalityChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    subCity: String,
    onSubCityChange: (String) -> Unit,
    email: String,
    phone: String,
    onNext: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Personal Details", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "Enter your exact name and details as displayed on your official ID.", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text("Full Legal Name") },
                placeholder = { Text("e.g. Legal Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = kycTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Date of Birth (Day / Month / Year)
            Text(text = "Date of Birth (DD / MM / YYYY)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dobDay,
                    onValueChange = onDobDayChange,
                    label = { Text("Day") },
                    modifier = Modifier.weight(1f),
                    colors = kycTextFieldColors()
                )
                OutlinedTextField(
                    value = dobMonth,
                    onValueChange = onDobMonthChange,
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f),
                    colors = kycTextFieldColors()
                )
                OutlinedTextField(
                    value = dobYear,
                    onValueChange = onDobYearChange,
                    label = { Text("Year") },
                    modifier = Modifier.weight(1.5f),
                    colors = kycTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nationality
            OutlinedTextField(
                value = nationality,
                onValueChange = onNationalityChange,
                label = { Text("Nationality / Country") },
                leadingIcon = { Text(text = "🇪🇹", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = kycTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // City & Sub-City
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("City") },
                    modifier = Modifier.weight(1f),
                    colors = kycTextFieldColors()
                )
                OutlinedTextField(
                    value = subCity,
                    onValueChange = onSubCityChange,
                    label = { Text("Sub-City / Woreda") },
                    modifier = Modifier.weight(1.5f),
                    colors = kycTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Verified Contact Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CryptoCardVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Linked Profile: $phone", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = email, color = TextSecondary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onNext,
                enabled = fullName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RealGoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Continue to Document Selection", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// =========================================================================
// STEP 2: DOCUMENT SELECTION
// =========================================================================
@Composable
fun KycStepDocumentSelect(
    selectedType: KycDocType,
    onTypeSelect: (KycDocType) -> Unit,
    idNumber: String,
    onIdNumberChange: (String) -> Unit,
    issuingAuthority: String,
    onIssuingAuthorityChange: (String) -> Unit,
    expiryDate: String,
    onExpiryDateChange: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Select Document Type", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "Choose an official, unexpired government document to verify.", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(14.dp))

            // Document Type Selection Cards
            KycDocType.values().forEach { docType ->
                val isSelected = docType == selectedType
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) RealGoldPrimary.copy(alpha = 0.08f) else CryptoCardVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(if (isSelected) RealGoldPrimary else CryptoCardBorder)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onTypeSelect(docType) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = docType.icon,
                                contentDescription = null,
                                tint = if (isSelected) RealGoldPrimary else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = docType.displayName,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) RealGoldPrimary else CryptoCardBorder,
                                    shape = CircleShape
                                )
                                .background(if (isSelected) RealGoldPrimary else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Document Number
            OutlinedTextField(
                value = idNumber,
                onValueChange = onIdNumberChange,
                label = { Text("Document ID Number") },
                placeholder = { Text(selectedType.placeholder) },
                modifier = Modifier.fillMaxWidth(),
                colors = kycTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Issuing Authority & Expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = issuingAuthority,
                    onValueChange = onIssuingAuthorityChange,
                    label = { Text("Issuing Authority") },
                    modifier = Modifier.weight(1.3f),
                    colors = kycTextFieldColors()
                )
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = onExpiryDateChange,
                    label = { Text("Expiry (MM/YY)") },
                    modifier = Modifier.weight(1f),
                    colors = kycTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(CryptoCardBorder)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Previous", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onNext,
                    enabled = idNumber.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RealGoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                ) {
                    Text(text = "Continue to Uploads", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// =========================================================================
// STEP 3: DOCUMENT UPLOADS & SELFIE
// =========================================================================
@Composable
fun KycStepDocumentUploads(
    selectedType: KycDocType,
    frontUploaded: Boolean,
    onFrontUpload: () -> Unit,
    onFrontRemove: () -> Unit,
    frontFileName: String,
    backUploaded: Boolean,
    onBackUpload: () -> Unit,
    onBackRemove: () -> Unit,
    backFileName: String,
    selfieUploaded: Boolean,
    onSelfieUpload: () -> Unit,
    onSelfieRemove: () -> Unit,
    selfieFileName: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Document Photos & Liveness", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "Upload high-resolution photos of your ${selectedType.displayName} and take a face selfie.", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // UPLOAD A: FRONT SIDE
            DocumentUploadCard(
                title = "Front Side of Document",
                instructions = "Ensure name, ID number, and photo are clear with no flash glare.",
                isUploaded = frontUploaded,
                fileName = frontFileName,
                fileSize = "2.4 MB",
                icon = Icons.Default.Badge,
                onUpload = onFrontUpload,
                onRemove = onFrontRemove
            )

            Spacer(modifier = Modifier.height(14.dp))

            // UPLOAD B: BACK SIDE
            DocumentUploadCard(
                title = "Back Side of Document",
                instructions = "Make sure barcode, signature, and issuing stamps are fully visible.",
                isUploaded = backUploaded,
                fileName = backFileName,
                fileSize = "1.9 MB",
                icon = Icons.Default.CreditCard,
                onUpload = onBackUpload,
                onRemove = onBackRemove
            )

            Spacer(modifier = Modifier.height(14.dp))

            // UPLOAD C: SELFIE WITH ID
            DocumentUploadCard(
                title = "Selfie with Document (Liveness Check)",
                instructions = "Take a clear portrait holding your ID next to your face in bright lighting.",
                isUploaded = selfieUploaded,
                fileName = selfieFileName,
                fileSize = "1.5 MB",
                icon = Icons.Default.Face,
                onUpload = onSelfieUpload,
                onRemove = onSelfieRemove
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Guidelines box
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = RealGoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Photo Guidelines for Fast Approval", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• Capture all four corners of the ID card", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "• Avoid reflections, blur, or dark shadows", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "• Black and white photocopies or scans are rejected", color = TextSecondary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(CryptoCardBorder)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Previous", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onNext,
                    enabled = frontUploaded && backUploaded,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RealGoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                ) {
                    Text(text = "Review & Submit", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun DocumentUploadCard(
    title: String,
    instructions: String,
    isUploaded: Boolean,
    fileName: String,
    fileSize: String,
    icon: ImageVector,
    onUpload: () -> Unit,
    onRemove: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            if (isUploaded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Attached", color = CryptoGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isUploaded) CryptoGreen.copy(alpha = 0.05f) else CryptoCardVariant)
                .border(
                    width = 1.dp,
                    color = if (isUploaded) CryptoGreen else CryptoCardBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
        ) {
            if (isUploaded) {
                // Attached State View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CryptoGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = fileName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "$fileSize • Ready for inspection", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onUpload, modifier = Modifier.size(34.dp)) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Retake", tint = RealGoldPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = CryptoRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                // Empty Upload Prompt
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpload() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RealGoldPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Upload", tint = RealGoldPrimary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tap to capture or upload photo", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = instructions, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// =========================================================================
// STEP 4: REVIEW & SUBMIT
// =========================================================================
@Composable
fun KycStepReviewAndSubmit(
    fullName: String,
    dob: String,
    nationality: String,
    city: String,
    docType: String,
    docNumber: String,
    frontUploaded: Boolean,
    backUploaded: Boolean,
    selfieUploaded: Boolean,
    termsAgreed: Boolean,
    onTermsAgreedChange: (Boolean) -> Unit,
    infoAccurateAgreed: Boolean,
    onInfoAccurateChange: (Boolean) -> Unit,
    autoApproveDemo: Boolean,
    onAutoApproveDemoChange: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CryptoCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CryptoCardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Review & Confirm Details", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "Please verify that all submitted information matches your physical documents.", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Table
            Card(
                colors = CardDefaults.cardColors(containerColor = CryptoCardVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    ReviewItemRow(label = "Legal Name", value = fullName)
                    ReviewItemRow(label = "Date of Birth", value = dob)
                    ReviewItemRow(label = "Nationality", value = nationality)
                    ReviewItemRow(label = "Address", value = city)
                    ReviewItemRow(label = "Document Type", value = docType)
                    ReviewItemRow(label = "ID Number", value = docNumber)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.HorizontalDivider(color = CryptoCardBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "ATTACHED DOCUMENTS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DocBadge("Front ID", frontUploaded)
                        DocBadge("Back ID", backUploaded)
                        DocBadge("Selfie", selfieUploaded)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Agreement Checkboxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = infoAccurateAgreed,
                    onCheckedChange = onInfoAccurateChange,
                    colors = CheckboxDefaults.colors(checkedColor = RealGoldPrimary, checkmarkColor = Color.Black)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "I certify that all details and documents provided are genuine and belong to me.",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAgreed,
                    onCheckedChange = onTermsAgreedChange,
                    colors = CheckboxDefaults.colors(checkedColor = RealGoldPrimary, checkmarkColor = Color.Black)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "I accept the Real Coin AML/CFT compliance terms and identity verification policy.",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Demo Instant Approval Switch
            Card(
                colors = CardDefaults.cardColors(containerColor = RealGoldPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoApproveDemo,
                        onCheckedChange = onAutoApproveDemoChange,
                        colors = CheckboxDefaults.colors(checkedColor = RealGoldPrimary, checkmarkColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(text = "Demo Mode: Instant Auto-Approve", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Unchecked: sets status to 'PENDING (In Review)'. Checked: sets to 'APPROVED'.", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(CryptoCardBorder)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Previous", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onSubmit,
                    enabled = termsAgreed && infoAccurateAgreed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RealGoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("submit_kyc_button")
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Submit KYC", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReviewItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DocBadge(label: String, isReady: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isReady) CryptoGreen.copy(alpha = 0.15f) else CryptoRed.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label: ${if (isReady) "✓" else "✕"}",
            color = if (isReady) CryptoGreen else CryptoRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun kycTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RealGoldPrimary,
    unfocusedBorderColor = CryptoCardBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = RealGoldPrimary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = RealGoldPrimary
)
