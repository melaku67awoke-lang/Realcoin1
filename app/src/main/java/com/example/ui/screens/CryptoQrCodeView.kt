package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest
import kotlin.math.abs

/**
 * Renders a crisp crypto QR code pattern for the BEP20 address
 * featuring a white rounded card with the signature cyan Tether center badge,
 * matching the user's uploaded screenshot.
 */
@Composable
fun BscQrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    // Generate deterministic 21x21 QR code matrix based on the address
    val matrix = remember(data) {
        generateQrMatrix(data, 21)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 28.dp)) {
            val n = matrix.size
            val cellSize = this.size.width / n

            // Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left)
            // Function to draw finder pattern
            fun drawFinder(cellX: Int, cellY: Int) {
                val startX = cellX * cellSize
                val startY = cellY * cellSize
                val outerSize = 7 * cellSize
                // Outer black ring
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(startX, startY),
                    size = Size(outerSize, outerSize),
                    cornerRadius = CornerRadius(cellSize * 1.2f, cellSize * 1.2f)
                )
                // Middle white square
                val midOffset = 1 * cellSize
                val midSize = 5 * cellSize
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(startX + midOffset, startY + midOffset),
                    size = Size(midSize, midSize),
                    cornerRadius = CornerRadius(cellSize * 0.8f, cellSize * 0.8f)
                )
                // Center black block
                val innerOffset = 2 * cellSize
                val innerSize = 3 * cellSize
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(startX + innerOffset, startY + innerOffset),
                    size = Size(innerSize, innerSize),
                    cornerRadius = CornerRadius(cellSize * 0.5f, cellSize * 0.5f)
                )
            }

            // Draw data cells (avoiding finder patterns and center logo area)
            val centerStart = 8
            val centerEnd = 12

            for (r in 0 until n) {
                for (c in 0 until n) {
                    // Check if inside finder patterns
                    val isTopLeft = r < 7 && c < 7
                    val isTopRight = r < 7 && c >= n - 7
                    val isBottomLeft = r >= n - 7 && c < 7
                    val isCenter = r in centerStart..centerEnd && c in centerStart..centerEnd

                    if (isTopLeft || isTopRight || isBottomLeft || isCenter) {
                        continue
                    }

                    if (matrix[r][c]) {
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.94f, cellSize * 0.94f),
                            cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
                        )
                    }
                }
            }

            // Draw finders
            drawFinder(0, 0)
            drawFinder(n - 7, 0)
            drawFinder(0, n - 7)
        }

        // Center Tether Logo Badge matching Screenshot exactly
        Box(
            modifier = Modifier
                .size(size * 0.22f)
                .clip(CircleShape)
                .background(Color.White)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Color(0xFF26A17B)), // Official Tether Teal
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₮",
                    color = Color.White,
                    fontSize = (size.value * 0.12f).sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private fun generateQrMatrix(input: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) }
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(input.toByteArray())

    var bitIndex = 0
    for (r in 0 until size) {
        for (c in 0 until size) {
            val byteVal = hash[(bitIndex / 8) % hash.size].toInt()
            val bit = (byteVal shr (bitIndex % 8)) and 1
            matrix[r][c] = bit == 1 || (r * c + r + c) % 3 == 0
            bitIndex++
        }
    }
    return matrix
}
