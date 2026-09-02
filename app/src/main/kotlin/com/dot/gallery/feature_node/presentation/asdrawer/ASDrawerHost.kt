/*
 * AS Team unified drawer implementation for AS-Negar.
 *
 * This layer intentionally sits above the existing ReFra navigation host so the
 * upstream gallery routes remain intact while AS-specific navigation is added.
 */
package com.dot.gallery.feature_node.presentation.asdrawer

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.dot.gallery.BuildConfig
import com.dot.gallery.core.branding.ASBrand
import com.dot.gallery.feature_node.presentation.util.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PROFILE_PREFS = "as_team_profile"
private const val PROFILE_IMAGE_URI = "profile_image_uri"

private enum class ASInfoDialog {
    ABOUT,
    CONTACT,
}

/**
 * میزبان منوی همبرگری مشترک AS Team.
 *
 * Drawer با LayoutDirection راست‌به‌چپ ساخته می‌شود تا از سمت راست باز شود؛
 * محتوای اصلی برنامه مجدداً LTR می‌شود تا رفتار صفحات upstream تغییر نکند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ASDrawerHost(
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeDialog by remember { mutableStateOf<ASInfoDialog?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                ASDrawerSheet(
                    onDestinationSelected = { destination ->
                        scope.launch { drawerState.close() }
                        when (destination) {
                            ASDrawerDestination.HOME -> navigateHome(navController)
                            ASDrawerDestination.SETTINGS -> navigateSettings(navController)
                            ASDrawerDestination.SHARE -> shareApp(context)
                            ASDrawerDestination.ABOUT -> activeDialog = ASInfoDialog.ABOUT
                            ASDrawerDestination.CONTACT -> activeDialog = ASInfoDialog.CONTACT
                        }
                    }
                )
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 4.dp, end = 8.dp),
                        onClick = { scope.launch { drawerState.open() } }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "باز کردن منوی نگار"
                        )
                    }
                }
            }
        }
    }

    when (activeDialog) {
        ASInfoDialog.ABOUT -> AboutDialog(onDismiss = { activeDialog = null })
        ASInfoDialog.CONTACT -> ContactDialog(
            onDismiss = { activeDialog = null },
            onEmail = { openSupportEmail(context) },
        )
        null -> Unit
    }
}

@Composable
private fun ASDrawerSheet(
    onDestinationSelected: (ASDrawerDestination) -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 340.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            ASProfileHeader()
            Spacer(modifier = Modifier.height(12.dp))
            Divider()

            ASDrawerItem(
                title = "خانه",
                icon = Icons.Outlined.Home,
                onClick = { onDestinationSelected(ASDrawerDestination.HOME) }
            )
            ASDrawerItem(
                title = "تنظیمات",
                icon = Icons.Outlined.Settings,
                onClick = { onDestinationSelected(ASDrawerDestination.SETTINGS) }
            )
            ASDrawerItem(
                title = "اشتراک‌گذاری برنامه",
                icon = Icons.Outlined.Share,
                onClick = { onDestinationSelected(ASDrawerDestination.SHARE) }
            )
            ASDrawerItem(
                title = "درباره نرم‌افزار",
                icon = Icons.Outlined.Info,
                onClick = { onDestinationSelected(ASDrawerDestination.ABOUT) }
            )
            ASDrawerItem(
                title = "تماس با ما",
                icon = Icons.Outlined.Email,
                onClick = { onDestinationSelected(ASDrawerDestination.CONTACT) }
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.padding(bottom = 20.dp),
                text = "Develop by ${ASBrand.DEVELOPER_NAME}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Header مشترک AS Team. تصویر انتخاب‌شده با OpenDocument انتخاب می‌شود تا
 * اجازه خواندن URI قابل نگهداری باشد و انتخاب کاربر بعد از اجرای مجدد حفظ شود.
 */
@Composable
private fun ASProfileHeader() {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
    }
    var selectedUri by remember {
        mutableStateOf(prefs.getString(PROFILE_IMAGE_URI, null)?.let(Uri::parse))
    }
    val profileBitmap by rememberProfileBitmap(selectedUri)

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                prefs.edit { putString(PROFILE_IMAGE_URI, uri.toString()) }
                selectedUri = uri
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .clickable(
                    role = Role.Button,
                    onClick = { picker.launch(arrayOf("image/*")) }
                ),
            contentAlignment = Alignment.Center,
        ) {
            val bitmap = profileBitmap
            if (bitmap != null) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = bitmap,
                    contentDescription = "تصویر پروفایل",
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    modifier = Modifier.size(76.dp),
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "انتخاب تصویر پروفایل",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(26.dp),
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = ASBrand.APP_NAME_FA,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = ASBrand.APP_NAME,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Decode the persisted profile URI off the main thread. */
@Composable
private fun rememberProfileBitmap(uri: Uri?): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(
        initialValue = null,
        key1 = uri,
    ) {
        value = if (uri == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        BitmapFactory.decodeStream(input)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }
}

@Composable
private fun ASDrawerItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        headlineContent = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Start,
            )
        },
        leadingContent = {
            Icon(imageVector = icon, contentDescription = null)
        }
    )
}

private fun navigateHome(navController: NavHostController) {
    navController.navigate(Screen.TimelineScreen.route) {
        launchSingleTop = true
        restoreState = true
    }
}

private fun navigateSettings(navController: NavHostController) {
    navController.navigate(Screen.SettingsScreen.route) {
        launchSingleTop = true
    }
}

private fun shareApp(context: Context) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, ASBrand.SHARE_TEXT)
    }
    context.startActivity(
        Intent.createChooser(sendIntent, "اشتراک‌گذاری ${ASBrand.APP_NAME_FA}")
    )
}

private fun openSupportEmail(context: Context) {
    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:${ASBrand.SUPPORT_EMAIL}")
        putExtra(Intent.EXTRA_SUBJECT, "${ASBrand.APP_NAME} Support")
    }
    runCatching { context.startActivity(mailIntent) }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        },
        title = { Text("درباره ${ASBrand.APP_NAME_FA}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(ASBrand.ABOUT_TEXT_FA)
                Text("نسخه: ${BuildConfig.VERSION_NAME}")
                Text("Develop by ${ASBrand.DEVELOPER_NAME}")
            }
        }
    )
}

@Composable
private fun ContactDialog(
    onDismiss: () -> Unit,
    onEmail: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onEmail) { Text("ارسال ایمیل") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        },
        title = { Text("تماس با ما") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("پشتیبانی ${ASBrand.APP_NAME_FA}")
                Text(ASBrand.SUPPORT_EMAIL)
                Text(ASBrand.DEVELOPER_NAME)
            }
        }
    )
}
