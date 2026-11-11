package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AchievementBadge
import com.example.data.model.BadgeCategory
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.VividPurple
import com.example.util.ShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun parseColorHex(hex: String, fallback: Color = NeonLime): Color {
    return runCatching {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    }.getOrDefault(fallback)
}

fun getBadgeIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "early_bird" -> Icons.Filled.WbSunny
        "marathoner" -> Icons.Filled.DirectionsRun
        "night_owl" -> Icons.Filled.Bedtime
        "step_master" -> Icons.Filled.DirectionsWalk
        "century_sprint" -> Icons.Filled.MilitaryTech
        "hydro_master" -> Icons.Filled.WaterDrop
        "calorie_crusher" -> Icons.Filled.LocalFireDepartment
        "iron_will" -> Icons.Filled.FitnessCenter
        else -> Icons.Filled.EmojiEvents
    }
}

@Composable
fun AchievementBadgesSection(
    badges: List<AchievementBadge>,
    unlockedCount: Int,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<BadgeCategory?>(null) }
    var showOnlyUnlocked by remember { mutableStateOf(false) }
    var selectedBadgeForDialog by remember { mutableStateOf<AchievementBadge?>(null) }

    val filteredBadges = remember(badges, selectedCategory, showOnlyUnlocked) {
        badges.filter { badge ->
            val matchesCategory = selectedCategory == null || badge.category == selectedCategory
            val matchesUnlocked = !showOnlyUnlocked || badge.isUnlocked
            matchesCategory && matchesUnlocked
        }
    }

    val totalCount = badges.size.coerceAtLeast(1)
    val overallProgress = (unlockedCount.toFloat() / totalCount).coerceIn(0f, 1f)

    val rankTitle = when {
        unlockedCount >= 7 -> "Level 4: Master of Iron 👑"
        unlockedCount >= 5 -> "Level 3: Elite Pulse Athlete ⚡"
        unlockedCount >= 3 -> "Level 2: Fitness Warrior 🔥"
        unlockedCount >= 1 -> "Level 1: Novice Mover 🌟"
        else -> "Level 0: Starter 🚀"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("achievement_badges_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VIRTUAL ACHIEVEMENTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonLime
                )
                Text(
                    text = "Earn badges & crush milestones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF202634))
                    .border(1.dp, NeonLime.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$unlockedCount / ${badges.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // Gamified Level Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gamified_level_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0x33CCFF00),
                                Color(0x1500E5FF),
                                Color(0x1012151B)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = rankTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Overall Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(overallProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonLime
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonLime,
                            trackColor = Color(0x22FFFFFF)
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null && !showOnlyUnlocked,
                    onClick = {
                        selectedCategory = null
                        showOnlyUnlocked = false
                    },
                    label = { Text("All (${badges.size})", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonLime,
                        selectedLabelColor = Color(0xFF0F1218)
                    )
                )
            }

            item {
                FilterChip(
                    selected = showOnlyUnlocked,
                    onClick = { showOnlyUnlocked = !showOnlyUnlocked },
                    label = { Text("Unlocked ($unlockedCount)", fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = Color(0xFF0F1218)
                    )
                )
            }

            items(BadgeCategory.entries.filter { it != BadgeCategory.ALL }) { cat ->
                val count = badges.count { it.category == cat }
                FilterChip(
                    selected = selectedCategory == cat && !showOnlyUnlocked,
                    onClick = {
                        selectedCategory = if (selectedCategory == cat) null else cat
                        showOnlyUnlocked = false
                    },
                    label = { Text("${cat.displayName} ($count)", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonLime,
                        selectedLabelColor = Color(0xFF0F1218)
                    )
                )
            }
        }

        // Badges Grid / List
        if (filteredBadges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No achievements match your filter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredBadges.forEach { badge ->
                    AchievementBadgeRowCard(
                        badge = badge,
                        onClick = { selectedBadgeForDialog = badge }
                    )
                }
            }
        }
    }

    // Detail Dialog
    selectedBadgeForDialog?.let { badge ->
        AchievementDetailDialog(
            badge = badge,
            onDismiss = { selectedBadgeForDialog = null }
        )
    }
}

@Composable
fun AchievementBadgeRowCard(
    badge: AchievementBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = parseColorHex(badge.accentColorHex)
    val icon = getBadgeIcon(badge.iconKey)

    val infiniteTransition = rememberInfiniteTransition(label = "badge_halo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("badge_card_${badge.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (badge.isUnlocked) accentColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medal Icon Container
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .scale(if (badge.isUnlocked) pulseScale else 1.0f)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) {
                            accentColor.copy(alpha = 0.2f)
                        } else {
                            Color(0x18FFFFFF)
                        }
                    )
                    .border(
                        1.5.dp,
                        if (badge.isUnlocked) accentColor else Color(0x33FFFFFF),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = badge.title,
                    tint = if (badge.isUnlocked) accentColor else Color(0x88FFFFFF),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    if (badge.isUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "UNLOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${(badge.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar or Unlock Date
                if (badge.isUnlocked) {
                    val formattedDate = badge.unlockedTimestamp?.let {
                        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Achieved"

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Unlocked $formattedDate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${badge.currentValue} / ${badge.targetValue} ${badge.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { badge.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = Color(0x22FFFFFF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementDetailDialog(
    badge: AchievementBadge,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = parseColorHex(badge.accentColorHex)
    val icon = getBadgeIcon(badge.iconKey)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("achievement_detail_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF161920),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Medal Showcase
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.35f),
                                    accentColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(2.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = badge.title,
                        tint = if (badge.isUnlocked) accentColor else Color(0x99FFFFFF),
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Badge Title
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge.category.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Card inside Dialog
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E232E))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (badge.isUnlocked) "Status: Completed" else "Status: In Progress",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) accentColor else Color(0xFF94A3B8)
                            )

                            Text(
                                text = "${badge.currentValue} / ${badge.targetValue} ${badge.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { badge.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accentColor,
                            trackColor = Color(0x22FFFFFF)
                        )

                        badge.unlockedTimestamp?.let { ts ->
                            Spacer(modifier = Modifier.height(10.dp))
                            val dateStr = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(ts))
                            Text(
                                text = "🏆 Unlocked on $dateStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Share Button & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }

                    if (badge.isUnlocked) {
                        Button(
                            onClick = {
                                ShareHelper.shareAchievement(
                                    context = context,
                                    title = badge.title,
                                    description = badge.description,
                                    category = badge.category.displayName
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_badge_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color(0xFF0F1218)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
