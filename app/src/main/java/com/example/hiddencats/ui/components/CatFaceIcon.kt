package com.example.hiddencats.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CatFaceIcon(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    catColor: Color = Color(0xFFF97316),
    eyeColor: Color = Color(0xFF0F172A),
    innerEarColor: Color = Color(0xFFF472B6)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h * 0.55f
        val headRadius = w * 0.35f
        val earSize = headRadius * 0.7f

        // Left ear
        val leftEar = Path().apply {
            moveTo(cx - headRadius * 0.75f, cy - headRadius * 0.2f)
            lineTo(cx - headRadius * 0.65f, cy - headRadius - earSize)
            lineTo(cx - headRadius * 0.1f, cy - headRadius * 0.7f)
            close()
        }
        drawPath(leftEar, catColor, style = Fill)
        drawPath(leftEar, Color(0xFF1C1917), style = Stroke(width = 2.5f))

        // Left inner ear
        val leftInner = Path().apply {
            moveTo(cx - headRadius * 0.65f, cy - headRadius * 0.25f)
            lineTo(cx - headRadius * 0.6f, cy - headRadius - earSize * 0.65f)
            lineTo(cx - headRadius * 0.2f, cy - headRadius * 0.65f)
            close()
        }
        drawPath(leftInner, innerEarColor, style = Fill)

        // Right ear
        val rightEar = Path().apply {
            moveTo(cx + headRadius * 0.1f, cy - headRadius * 0.7f)
            lineTo(cx + headRadius * 0.65f, cy - headRadius - earSize)
            lineTo(cx + headRadius * 0.75f, cy - headRadius * 0.2f)
            close()
        }
        drawPath(rightEar, catColor, style = Fill)
        drawPath(rightEar, Color(0xFF1C1917), style = Stroke(width = 2.5f))

        // Right inner ear
        val rightInner = Path().apply {
            moveTo(cx + headRadius * 0.2f, cy - headRadius * 0.65f)
            lineTo(cx + headRadius * 0.6f, cy - headRadius - earSize * 0.65f)
            lineTo(cx + headRadius * 0.65f, cy - headRadius * 0.25f)
            close()
        }
        drawPath(rightInner, innerEarColor, style = Fill)

        // Head
        drawCircle(color = catColor, radius = headRadius, center = Offset(cx, cy))
        drawCircle(color = Color(0xFF1C1917), radius = headRadius, center = Offset(cx, cy), style = Stroke(width = 2.5f))

        // Eyes
        val eyeSpacing = headRadius * 0.38f
        val eyeY = cy - headRadius * 0.08f
        val eyeRadius = headRadius * 0.18f
        drawCircle(color = eyeColor, radius = eyeRadius, center = Offset(cx - eyeSpacing, eyeY))
        drawCircle(color = eyeColor, radius = eyeRadius, center = Offset(cx + eyeSpacing, eyeY))
        // Catchlights
        drawCircle(color = Color.White, radius = eyeRadius * 0.35f, center = Offset(cx - eyeSpacing - 1.5f, eyeY - 1.5f))
        drawCircle(color = Color.White, radius = eyeRadius * 0.35f, center = Offset(cx + eyeSpacing - 1.5f, eyeY - 1.5f))

        // Nose
        val noseY = cy + headRadius * 0.18f
        drawCircle(color = innerEarColor, radius = headRadius * 0.12f, center = Offset(cx, noseY))

        // Whiskers
        val whiskerLen = headRadius * 0.5f
        drawLine(Color(0xFF1C1917), Offset(cx - headRadius * 0.25f, noseY), Offset(cx - headRadius * 0.25f - whiskerLen, noseY - 2f), strokeWidth = 1.5f)
        drawLine(Color(0xFF1C1917), Offset(cx - headRadius * 0.25f, noseY + 3f), Offset(cx - headRadius * 0.25f - whiskerLen, noseY + 5f), strokeWidth = 1.5f)
        drawLine(Color(0xFF1C1917), Offset(cx + headRadius * 0.25f, noseY), Offset(cx + headRadius * 0.25f + whiskerLen, noseY - 2f), strokeWidth = 1.5f)
        drawLine(Color(0xFF1C1917), Offset(cx + headRadius * 0.25f, noseY + 3f), Offset(cx + headRadius * 0.25f + whiskerLen, noseY + 5f), strokeWidth = 1.5f)
    }
}

@Composable
fun CatPawIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color(0xFFF97316)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h * 0.6f

        // Main pad
        drawCircle(tint, radius = w * 0.24f, center = Offset(cx, cy))

        // 4 Toe pads
        val toeR = w * 0.1f
        drawCircle(tint, radius = toeR, center = Offset(cx - w * 0.28f, cy - h * 0.28f))
        drawCircle(tint, radius = toeR, center = Offset(cx - w * 0.1f, cy - h * 0.38f))
        drawCircle(tint, radius = toeR, center = Offset(cx + w * 0.1f, cy - h * 0.38f))
        drawCircle(tint, radius = toeR, center = Offset(cx + w * 0.28f, cy - h * 0.28f))
    }
}
