package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.CharcoalSecondary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.IvorySurfaceLight

sealed class DrawerDestination {
  object Home : DrawerDestination()
  object Events : DrawerDestination()
  object Guests : DrawerDestination()
  object CreateDesign : DrawerDestination()
  object DesignLibrary : DrawerDestination()
  object CreateAnimation : DrawerDestination()
  object AnimationLibrary : DrawerDestination()
  object Settings : DrawerDestination()
  object About : DrawerDestination()
}

@Composable
fun InvioraDrawerContent(
  currentDestination: DrawerDestination,
  onSelectDestination: (DrawerDestination) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxHeight()
      .width(320.dp)
      .background(IvoryBg)
      .border(width = 0.5.dp, color = IvoryBorder)
      .statusBarsPadding()
      .padding(vertical = 16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Inviora",
            fontFamily = FontFamily.Serif,
            fontSize = 22.sp,
            letterSpacing = 2.sp,
            color = CharcoalPrimary
          )
          Text(
            text = "Creative Studio",
            fontSize = 11.sp,
            color = GoldDark,
            letterSpacing = 1.sp
          )
        }

        IconButton(
          onClick = onClose,
          modifier = Modifier.testTag("drawer_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Menu",
            tint = CharcoalMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = IvoryBorder, thickness = 0.5.dp)
      Spacer(modifier = Modifier.height(16.dp))

      // Section: Creative Studio
      SectionHeader(title = "CREATIVE STUDIO")

      DrawerItem(
        title = "Create Design",
        subtitle = "From visual reference & prompt",
        icon = Icons.Default.AutoAwesome,
        selected = currentDestination == DrawerDestination.CreateDesign,
        tag = "drawer_create_design",
        onClick = { onSelectDestination(DrawerDestination.CreateDesign) }
      )

      DrawerItem(
        title = "Design Library",
        subtitle = "Reusable invitation designs",
        icon = Icons.Default.FolderSpecial,
        selected = currentDestination == DrawerDestination.DesignLibrary,
        tag = "drawer_design_library",
        onClick = { onSelectDestination(DrawerDestination.DesignLibrary) }
      )

      DrawerItem(
        title = "Create Animation",
        subtitle = "From reference motion physics",
        icon = Icons.Default.Animation,
        selected = currentDestination == DrawerDestination.CreateAnimation,
        tag = "drawer_create_animation",
        onClick = { onSelectDestination(DrawerDestination.CreateAnimation) }
      )

      DrawerItem(
        title = "Animation Library",
        subtitle = "Reusable reveal experiences",
        icon = Icons.Default.DesignServices,
        selected = currentDestination == DrawerDestination.AnimationLibrary,
        tag = "drawer_animation_library",
        onClick = { onSelectDestination(DrawerDestination.AnimationLibrary) }
      )

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = IvoryBorder, thickness = 0.5.dp)
      Spacer(modifier = Modifier.height(16.dp))

      // Section: Management
      SectionHeader(title = "MANAGEMENT")

      DrawerItem(
        title = "Home",
        subtitle = "Current active event",
        icon = Icons.Default.CalendarMonth,
        selected = currentDestination == DrawerDestination.Home,
        tag = "drawer_home",
        onClick = { onSelectDestination(DrawerDestination.Home) }
      )

      DrawerItem(
        title = "All Events",
        subtitle = "List and switch active events",
        icon = Icons.Default.FolderSpecial,
        selected = currentDestination == DrawerDestination.Events,
        tag = "drawer_events",
        onClick = { onSelectDestination(DrawerDestination.Events) }
      )

      DrawerItem(
        title = "All Guests",
        subtitle = "Links and guest management",
        icon = Icons.Default.People,
        selected = currentDestination == DrawerDestination.Guests,
        tag = "drawer_guests",
        onClick = { onSelectDestination(DrawerDestination.Guests) }
      )

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = IvoryBorder, thickness = 0.5.dp)
      Spacer(modifier = Modifier.height(16.dp))

      // Section: Preferences & About
      SectionHeader(title = "SETTINGS")

      DrawerItem(
        title = "Settings",
        subtitle = "AI keys & preferences",
        icon = Icons.Default.Settings,
        selected = currentDestination == DrawerDestination.Settings,
        tag = "drawer_settings",
        onClick = { onSelectDestination(DrawerDestination.Settings) }
      )

      DrawerItem(
        title = "About Inviora",
        subtitle = "Brand philosophy & version",
        icon = Icons.Default.Info,
        selected = currentDestination == DrawerDestination.About,
        tag = "drawer_about",
        onClick = { onSelectDestination(DrawerDestination.About) }
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    fontSize = 10.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 1.8.sp,
    color = GoldDark,
    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
  )
}

@Composable
private fun DrawerItem(
  title: String,
  subtitle: String,
  icon: ImageVector,
  selected: Boolean,
  tag: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(if (selected) IvorySurfaceLight else Color.Transparent)
      .border(
        width = if (selected) 0.8.dp else 0.dp,
        color = if (selected) GoldPrimary else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag(tag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = if (selected) GoldPrimary else CharcoalMuted,
      modifier = Modifier.size(20.dp)
    )

    Spacer(modifier = Modifier.width(14.dp))

    Column {
      Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) CharcoalPrimary else CharcoalSecondary
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = CharcoalMuted
      )
    }
  }
}
