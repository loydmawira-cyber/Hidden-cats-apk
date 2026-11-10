package com.example.hiddencats.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
        val outline = Color(0xFF2A211D)
        val fur = Brush.radialGradient(
            colors = listOf(catColor.lighten(0.28f), catColor, catColor.darken(0.38f)),
            center = Offset(cx - headRadius * 0.32f, cy - headRadius * 0.42f),
            radius = headRadius * 1.65f
        )

        // Left ear
        val leftEar = Path().apply {
            moveTo(cx - headRadius * 0.75f, cy - headRadius * 0.2f)
            lineTo(cx - headRadius * 0.65f, cy - headRadius - earSize)
            lineTo(cx - headRadius * 0.1f, cy - headRadius * 0.7f)
            close()
        }
        drawPath(leftEar, fur, style = Fill)
        drawPath(leftEar, outline, style = Stroke(width = 2.5f))

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
        drawPath(rightEar, fur, style = Fill)
        drawPath(rightEar, outline, style = Stroke(width = 2.5f))

        // Right inner ear
        val rightInner = Path().apply {
            moveTo(cx + headRadius * 0.2f, cy - headRadius * 0.65f)
            lineTo(cx + headRadius * 0.6f, cy - headRadius - earSize * 0.65f)
            lineTo(cx + headRadius * 0.65f, cy - headRadius * 0.25f)
            close()
        }
        drawPath(rightInner, innerEarColor, style = Fill)

        // Head
        drawCircle(brush = fur, radius = headRadius, center = Offset(cx, cy))
        drawCircle(color = outline, radius = headRadius, center = Offset(cx, cy), style = Stroke(width = 2.5f))
        // Soft tabby markings add depth so the revealed cat reads as a cat, not a flat badge.
        val stripe = outline.copy(alpha = 0.38f)
        for (offset in listOf(-0.24f, 0f, 0.24f)) {
            drawLine(
                stripe,
                Offset(cx + headRadius * offset, cy - headRadius * 0.82f),
                Offset(cx + headRadius * offset * 0.72f, cy - headRadius * 0.52f),
                strokeWidth = 1.8f
            )
        }

        // Eyes
        val eyeSpacing = headRadius * 0.38f
        val eyeY = cy - headRadius * 0.08f
        val eyeRadius = headRadius * 0.18f
        drawOval(brush = Brush.radialGradient(listOf(Color.White, eyeColor)), topLeft = Offset(cx - eyeSpacing - eyeRadius, eyeY - eyeRadius), size = Size(eyeRadius * 2f, eyeRadius * 2.25f))
        drawOval(brush = Brush.radialGradient(listOf(Color.White, eyeColor)), topLeft = Offset(cx + eyeSpacing - eyeRadius, eyeY - eyeRadius), size = Size(eyeRadius * 2f, eyeRadius * 2.25f))
        drawCircle(color = Color.Black.copy(alpha = 0.78f), radius = eyeRadius * 0.38f, center = Offset(cx - eyeSpacing, eyeY))
        drawCircle(color = Color.Black.copy(alpha = 0.78f), radius = eyeRadius * 0.38f, center = Offset(cx + eyeSpacing, eyeY))
        // Catchlights
        drawCircle(color = Color.White, radius = eyeRadius * 0.35f, center = Offset(cx - eyeSpacing - 1.5f, eyeY - 1.5f))
        drawCircle(color = Color.White, radius = eyeRadius * 0.35f, center = Offset(cx + eyeSpacing - 1.5f, eyeY - 1.5f))

        // Nose
        val noseY = cy + headRadius * 0.18f
        drawOval(color = Color.White.copy(alpha = 0.16f), topLeft = Offset(cx - headRadius * 0.34f, noseY - headRadius * 0.02f), size = Size(headRadius * 0.68f, headRadius * 0.34f))
        drawCircle(color = innerEarColor, radius = headRadius * 0.12f, center = Offset(cx, noseY))

        // Whiskers
        val whiskerLen = headRadius * 0.5f
        drawLine(outline, Offset(cx - headRadius * 0.25f, noseY), Offset(cx - headRadius * 0.25f - whiskerLen, noseY - 2f), strokeWidth = 1.5f)
        drawLine(outline, Offset(cx - headRadius * 0.25f, noseY + 3f), Offset(cx - headRadius * 0.25f - whiskerLen, noseY + 5f), strokeWidth = 1.5f)
        drawLine(outline, Offset(cx + headRadius * 0.25f, noseY), Offset(cx + headRadius * 0.25f + whiskerLen, noseY - 2f), strokeWidth = 1.5f)
        drawLine(outline, Offset(cx + headRadius * 0.25f, noseY + 3f), Offset(cx + headRadius * 0.25f + whiskerLen, noseY + 5f), strokeWidth = 1.5f)
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
