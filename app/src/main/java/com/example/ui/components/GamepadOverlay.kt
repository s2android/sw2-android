package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EglantineCyan
import com.example.ui.theme.EglantineMagenta
import com.example.ui.theme.EglantineViolet
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GamepadOverlay(
    modifier: Modifier = Modifier,
    opacity: Float = 0.75f,
    hapticsEnabled: Boolean = true,
    onButtonPressed: (String) -> Unit = {}
) {
    val context = LocalContext.current

    fun triggerHaptic() {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(15)
                }
            }
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(opacity)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // TOP SHOULDER TRIGGERS BAR (L, ZL, -, +, ZR, R)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Shoulder Buttons (ZL & L)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GamepadButton(
                    text = "ZL",
                    width = 64,
                    height = 36,
                    tag = "overlay_zl",
                    onPress = {
                        triggerHaptic()
                        onButtonPressed("ZL")
                    }
                )
                GamepadButton(
                    text = "L",
                    width = 54,
                    height = 36,
                    tag = "overlay_l",
                    onPress = {
                        triggerHaptic()
                        onButtonPressed("L")
                    }
                )
            }

            // Center System Buttons (Minus, Capture, Home, Plus)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButtonOverlay(
                    icon = Icons.Default.Remove,
                    tag = "overlay_minus",
                    onPress = {
                        triggerHaptic()
                        onButtonPressed("-")
                    }
                )
                IconButtonOverlay(
                    icon = Icons.Default.CameraAlt,
                    tag = "overlay_capture",
                    onPress = {
                        triggerHaptic()
                        onButtonPressed("CAPTURE")
                    }
                )
                IconButtonOverlay(
                    icon = Icons.Default.Home,
                    tag = "overlay_home",
                    onPress = {
                        triggerHaptic()
                        onButtonPressed("HOME")
                    }
                )
                IconButtonOverlay(
                    icon = Icons.Default.Add,
                    tag = "overlay_plus",
                    onPress = {
                        triggerHaptic()
                        onButtonPressed("+")
                    }
                )
            }

            // Right Shoulder Buttons (R & ZR)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GamepadButton(
                    text = "R",
                    width = 54,
                    height = 36,
                    tag = "overlay_r",
                    onPress = {
                        triggerHaptic()
                        onButtonPressed("R")
                    }
                )
                GamepadButton(
                    text = "ZR",
                    width = 64,
                    height = 36,
                    tag = "overlay_zr",
                    onPress = {
                        triggerHaptic()
                        onButtonPressed("ZR")
                    }
                )
            }
        }

        // BOTTOM CONTROLS: Left Side (Left Joystick + D-Pad) & Right Side (ABXY + Right Joystick)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // LEFT SIDE: Left Analog Stick & D-Pad
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Analog Stick
                VirtualJoystick(
                    label = "L-Stick",
                    tag = "overlay_joystick_left",
                    onMoved = { triggerHaptic() }
                )

                // D-Pad
                DpadControl(
                    onDirectionPress = { dir ->
                        triggerHaptic()
                        onButtonPressed("DPAD_$dir")
                    }
                )
            }

            // RIGHT SIDE: ABXY Action Buttons & Right Analog Stick
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ABXY Diamond Layout
                NintendoAbxyDiamond(
                    onPress = { btn ->
                        triggerHaptic()
                        onButtonPressed(btn)
                    }
                )

                // Right Analog Stick
                VirtualJoystick(
                    label = "R-Stick",
                    tag = "overlay_joystick_right",
                    onMoved = { triggerHaptic() }
                )
            }
        }
    }
}

@Composable
fun VirtualJoystick(
    label: String,
    tag: String,
    maxRadiusPx: Float = 60f,
    onMoved: () -> Unit = {}
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(Color(0x551A1828))
            .border(1.5.dp, Color(0x66E040FB), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = offsetX + dragAmount.x
                        val newY = offsetY + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)
                        val angle = atan2(newY, newX)
                        val clampedDist = min(dist, maxRadiusPx)
                        offsetX = clampedDist * cos(angle)
                        offsetY = clampedDist * sin(angle)
                        onMoved()
                    }
                )
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        // Inner thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xDD342B4E))
                .border(2.dp, EglantineViolet, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = EglantineCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DpadControl(
    onDirectionPress: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .testTag("overlay_dpad"),
        contentAlignment = Alignment.Center
    ) {
        // Center cross background
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(104.dp)
                .background(Color(0x66221E36), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x55E040FB), RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .width(104.dp)
                .height(36.dp)
                .background(Color(0x66221E36), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x55E040FB), RoundedCornerShape(8.dp))
        )

        // UP
        DpadButton(
            text = "▲",
            modifier = Modifier.align(Alignment.TopCenter),
            onPress = { onDirectionPress("UP") }
        )
        // DOWN
        DpadButton(
            text = "▼",
            modifier = Modifier.align(Alignment.BottomCenter),
            onPress = { onDirectionPress("DOWN") }
        )
        // LEFT
        DpadButton(
            text = "◀",
            modifier = Modifier.align(Alignment.CenterStart),
            onPress = { onDirectionPress("LEFT") }
        )
        // RIGHT
        DpadButton(
            text = "▶",
            modifier = Modifier.align(Alignment.CenterEnd),
            onPress = { onDirectionPress("RIGHT") }
        )
    }
}

@Composable
fun DpadButton(
    text: String,
    modifier: Modifier = Modifier,
    onPress: () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onPress() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NintendoAbxyDiamond(
    onPress: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .testTag("overlay_abxy"),
        contentAlignment = Alignment.Center
    ) {
        // X button at TOP
        AbxyCircleButton(
            label = "X",
            color = EglantineCyan,
            modifier = Modifier.align(Alignment.TopCenter),
            onPress = { onPress("X") }
        )
        // Y button at LEFT
        AbxyCircleButton(
            label = "Y",
            color = Color(0xFFFFD54F),
            modifier = Modifier.align(Alignment.CenterStart),
            onPress = { onPress("Y") }
        )
        // A button at RIGHT
        AbxyCircleButton(
            label = "A",
            color = Color(0xFFFF5252),
            modifier = Modifier.align(Alignment.CenterEnd),
            onPress = { onPress("A") }
        )
        // B button at BOTTOM
        AbxyCircleButton(
            label = "B",
            color = Color(0xFF69F0AE),
            modifier = Modifier.align(Alignment.BottomCenter),
            onPress = { onPress("B") }
        )
    }
}

@Composable
fun AbxyCircleButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (isPressed) color.copy(alpha = 0.5f) else Color(0x772A2242))
            .border(1.5.dp, color, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onPress()
            }
            .testTag("overlay_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun GamepadButton(
    text: String,
    width: Int,
    height: Int,
    tag: String,
    onPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x6629233F))
            .border(1.dp, Color(0x88E040FB), RoundedCornerShape(8.dp))
            .clickable { onPress() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun IconButtonOverlay(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0x6629233F))
            .border(1.dp, Color(0x66E040FB), CircleShape)
            .clickable { onPress() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tag,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}
