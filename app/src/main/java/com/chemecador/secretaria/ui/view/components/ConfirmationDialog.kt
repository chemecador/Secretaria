package com.chemecador.secretaria.ui.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.chemecador.secretaria.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    confirmIcon: ImageVector,
    dismissIcon: ImageVector,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.margin_large))) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.margin_small))
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_medium)))
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(
                            horizontal = dimensionResource(R.dimen.margin_small),
                            vertical = dimensionResource(R.dimen.margin_xsmall)
                        )
                    ) {
                        Icon(
                            imageVector = dismissIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                        Text(
                            dismissText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(
                        onClick = onConfirm,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(
                            horizontal = dimensionResource(R.dimen.margin_small),
                            vertical = dimensionResource(R.dimen.margin_xsmall)
                        )
                    ) {
                        Icon(
                            imageVector = confirmIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                        Text(
                            confirmText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}


