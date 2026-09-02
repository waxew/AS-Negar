/*
 * AS Team settings identity header for AS-Negar.
 *
 * The upstream donation sheet is intentionally not exposed from this product
 * header because its payment destinations belong to the upstream author and
 * must not be presented as AS Team support destinations.
 */
package com.dot.gallery.feature_node.presentation.settings.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dot.gallery.BuildConfig
import com.dot.gallery.core.branding.ASBrand

/**
 * هدر اصلی تنظیمات نگار.
 *
 * فقط مسیرهای رسمی AS Team را نمایش می‌دهد و هیچ مقصد پرداخت upstream را به
 * عنوان پشتیبانی نگار معرفی نمی‌کند.
 */
@Composable
fun ASSettingsAppHeader(
    onDismiss: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = ASBrand.APP_NAME_FA,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = ASBrand.APP_NAME,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Develop by ${ASBrand.DEVELOPER_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = ASBrand.SUPPORT_EMAIL,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = { openSupportEmail(context) },
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تماس با ما")
                }

                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = { openRepository(context) },
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GitHub")
                }
            }

            if (onDismiss != null) {
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onDismiss,
                ) {
                    Text("نمایش فشرده")
                }
            }
        }
    }
}

/** هدر فشرده‌ای که کاربر می‌تواند دوباره به حالت کامل برگرداند. */
@Composable
fun ASSettingsAppHeaderCompact(
    onRestore: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = ASBrand.APP_NAME_FA,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onRestore) {
                Text("نمایش اطلاعات")
            }
        }
    }
}

private fun openSupportEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:${ASBrand.SUPPORT_EMAIL}")
        putExtra(Intent.EXTRA_SUBJECT, "${ASBrand.APP_NAME} Support")
    }
    runCatching { context.startActivity(intent) }
}

private fun openRepository(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ASBrand.REPOSITORY_URL))
    runCatching { context.startActivity(intent) }
}
