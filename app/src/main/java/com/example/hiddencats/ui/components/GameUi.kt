package com.example.hiddencats.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.ui.theme.CatOrangePrimary
import com.example.hiddencats.ui.theme.DayBottom
import com.example.hiddencats.ui.theme.DayTop
import com.example.hiddencats.ui.theme.GoldStar
import com.example.hiddencats.ui.theme.InkOutline
import com.example.hiddencats.ui.theme.NightBottom
import com.example.hiddencats.ui.theme.NightTop
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun isDarkUi(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

fun Color.darken(fraction: Float): Color = lerp(this, Color.Black, fraction)
fun Color.lighten(fraction: Float): Color = lerp(this, Color.White, fraction)

/** Full-screen gradient with a faint scatter of paw prints. */
@Composable
fun GameBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isDarkUi()
    val top = if (dark) NightTop else DayTop
    val bottom = if (dark) NightBottom else DayBottom
    val pawColor = if (dark) Color.White.copy(alpha = 0.05f) else Color(0xFF7A3E00).copy(alpha = 0.07f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 132.dp.toPx()
            val paw = 34.dp.toPx()
            var row = 0
            var y = step * 0.4f
            while (y < size.height + step) {
                var x = if (row % 2 == 0) step * 0.3f else step * 0.8f
                while (x < size.width + step) {
                    rotate(degrees = if ((row + (x / step).toInt()) % 2 == 0) -22f else 18f, pivot = Offset(x, y)) {
                        drawPaw(x, y, paw, pawColor)
                    }
                    x += step
                }
                y += step * 0.9f
                row++
            }
        }
        content()
    }
}

private fun DrawScope.drawPaw(cx: Float, cy: Float, s: Float, color: Color) {
    drawCircle(color, radius = s * 0.26f, center = Offset(cx, cy + s * 0.12f))
    val toe = s * 0.11f
    drawCircle(color, toe, Offset(cx - s * 0.3f, cy - s * 0.12f))
    drawCircle(color, toe, Offset(cx - s * 0.11f, cy - s * 0.3f))
    drawCircle(color, toe, Offset(cx + s * 0.11f, cy - s * 0.3f))
    drawCircle(color, toe, Offset(cx + s * 0.3f, cy - s * 0.12f))
}

/** Chunky candy-style button with a 3D bottom edge that "presses" when tapped. */
@Composable
fun GameButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CatOrangePrimary,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    shape: Shape = RoundedCornerShape(18.dp),
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth = 6.dp
    val pressOffset by animateDpAsState(
        targetValue = if (pressed && enabled) depth else 0.dp,
        animationSpec = tween(70),
        label = "button_press"
    )
    val base = if (enabled) color else Color(0xFF9A9A9A)
    val edge = base.darken(0.32f)

    Box(
        modifier = modifier
            .height(height + depth)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .align(Alignment.BottomCenter)
                .clip(shape)
                .background(edge)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = pressOffset)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(base.lighten(0.22f), base)))
                .border(2.dp, edge, shape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun GameButtonText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    color: Color = Color.White
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp
    )
}

/** Thick-outlined card used for HUD blocks, option groups and result panels. */
@Composable
fun GamePanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = isDarkUi()
    val edge = if (dark) Color(0xFF8E7BE0).copy(alpha = 0.55f) else InkOutline.copy(alpha = 0.85f)
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(3.dp, edge),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** Small rounded status pill (hearts, timer, hint balance...). */
@Composable
fun StatPill(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dark = isDarkUi()
    val edge = if (dark) Color(0xFF8E7BE0).copy(alpha = 0.55f) else InkOutline.copy(alpha = 0.8f)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, edge),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { content() }
    }
}

/** Big game-logo style title: fill text on top of a thick rounded stroke. */
@Composable
fun OutlinedTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 38.sp,
    fill: Color = Color.White,
    outline: Color = InkOutline
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = outline,
                drawStyle = Stroke(width = fontSize.value * 0.42f, join = StrokeJoin.Round)
            )
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = fill
            )
        )
    }
}

/** Three stars that pop in one after another. */
@Composable
fun StarRow(
    stars: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 48.dp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (s in 1..3) {
            var shown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(250L + s * 260L)
                shown = true
            }
            val earned = s <= stars
            val scale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (shown) 1f else 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "star_$s"
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = if (earned) "Star $s earned" else "Star $s not earned",
                tint = if (earned) GoldStar else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(starSize)
                    .scale(scale)
            )
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val delay: Float,
    val speed: Float,
    val sway: Float,
    val size: Float,
    val spin: Float,
    val color: Color
)

/** One-shot confetti rain for victory screens. */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier) {
    val palette = remember {
        listOf(Color(0xFFFF6B6B), Color(0xFF4D96FF), Color(0xFF6BCB77), Color(0xFFFFD93D), Color(0xFF9D4EDD), Color(0xFFFF9F45))
    }
    val pieces = remember {
        val rnd = Random(7)
        List(46) {
            ConfettiPiece(
                x = rnd.nextFloat(),
                delay = rnd.nextFloat() * 0.35f,
                speed = 0.75f + rnd.nextFloat() * 0.5f,
                sway = 2f + rnd.nextFloat() * 5f,
                size = 7f + rnd.nextFloat() * 8f,
                spin = rnd.nextFloat() * 360f,
                color = palette[rnd.nextInt(palette.size)]
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(3200, easing = LinearEasing)) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        for (p in pieces) {
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f) continue
            val px = p.x * size.width + sin(local * p.sway * 3.14f) * 24.dp.toPx()
            val py = -20.dp.toPx() + local * p.speed * (size.height + 40.dp.toPx())
            rotate(degrees = p.spin + local * 540f, pivot = Offset(px, py)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(px - p.size.dp.toPx() / 2, py - p.size.dp.toPx() / 3),
                    size = Size(p.size.dp.toPx(), p.size.dp.toPx() * 0.62f)
                )
            }
        }
    }
}
