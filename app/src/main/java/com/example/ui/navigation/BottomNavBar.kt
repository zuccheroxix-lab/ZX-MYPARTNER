package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAccentColor

/**
 * 4 Primary Navigation Destinations:
 * 1. HOME
 * 2. GAME
 * 3. TOOLS
 * 4. SETTINGS
 */
enum class NavScreen(
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val tag: String,
    val pageIndex: Int
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home", 0),
    GAME("Game", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports, "nav_game", 1),
    TOOLS("Tools", Icons.Filled.Build, Icons.Outlined.Build, "nav_tools", 2),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings", 3);

    companion object {
        fun fromIndex(index: Int): NavScreen = when (index) {
            0 -> HOME
            1 -> GAME
            2 -> TOOLS
            3 -> SETTINGS
            else -> HOME
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: NavScreen,
    onSelectScreen: (NavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .testTag("floating_bottom_navigation")
    ) {
        // Floating Dark Navigation Container with 4 main items
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141418).copy(alpha = 0.96f))
                .border(
                    width = 0.8.dp,
                    color = Color(0xFF282832),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    val interactionSource = remember { MutableInteractionSource() }
                    val accentColor = LocalAccentColor.current

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onSelectScreen(screen) }
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag(screen.tag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) accentColor.copy(alpha = 0.16f) else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                contentDescription = screen.title,
                                tint = if (isSelected) accentColor else Color(0xFF757582),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) accentColor else Color(0xFF757582),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.5.sp
                            ),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) accentColor else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
