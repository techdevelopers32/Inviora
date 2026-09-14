package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvorySurface

enum class NavTab {
  HOME,
  EVENTS,
  GUESTS
}

@Composable
fun InvioraBottomNav(
  currentTab: NavTab,
  onTabSelected: (NavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(IvorySurface)
      .border(width = 0.5.dp, color = IvoryBorder)
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavItem(
        label = "Home",
        selected = currentTab == NavTab.HOME,
        activeIcon = Icons.Filled.Home,
        inactiveIcon = Icons.Outlined.Home,
        tag = "nav_home",
        onClick = { onTabSelected(NavTab.HOME) }
      )
      NavItem(
        label = "Events",
        selected = currentTab == NavTab.EVENTS,
        activeIcon = Icons.Filled.CalendarMonth,
        inactiveIcon = Icons.Outlined.CalendarMonth,
        tag = "nav_events",
        onClick = { onTabSelected(NavTab.EVENTS) }
      )
      NavItem(
        label = "Guests",
        selected = currentTab == NavTab.GUESTS,
        activeIcon = Icons.Filled.People,
        inactiveIcon = Icons.Outlined.People,
        tag = "nav_guests",
        onClick = { onTabSelected(NavTab.GUESTS) }
      )
    }
  }
}

@Composable
private fun NavItem(
  label: String,
  selected: Boolean,
  activeIcon: ImageVector,
  inactiveIcon: ImageVector,
  tag: String,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag(tag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = if (selected) activeIcon else inactiveIcon,
      contentDescription = label,
      tint = if (selected) GoldPrimary else CharcoalMuted,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
      color = if (selected) GoldDark else CharcoalMuted
    )
  }
}

