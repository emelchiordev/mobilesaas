package re.melchior.saviomobile.ui.refonte

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.SavioUi

@Composable
fun SavioClientInfoCard(
    addressLine: String,
    addressLine2: String,
    floor: String,
    doorCode: String,
    phone: String,
    email: String,
    notes: String,
    isEditing: Boolean,
    emailError: String?,
    onAddressLine2Change: (String) -> Unit,
    onFloorChange: (String) -> Unit,
    onDoorCodeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val addressDisplay = buildString {
        append(addressLine.ifBlank { "Adresse non renseignée" })
        if (addressLine2.isNotBlank() && !isEditing) {
            append("\n")
            append(addressLine2)
        }
        if (!isEditing) {
            val accessParts = buildList {
                if (floor.isNotBlank()) add("Étage $floor")
                if (doorCode.isNotBlank()) add("Code $doorCode")
            }
            if (accessParts.isNotEmpty()) {
                append("\n")
                append(accessParts.joinToString(" · "))
            }
        }
    }

    SavioRefonteCard(modifier = modifier) {
        SavioInfoRow(
            icon = Icons.Filled.LocationOn,
            label = "Accès logement",
            value = addressDisplay,
            showDivider = false,
        )
        if (!isEditing && addressLine.isNotBlank()) {
            SavioInfoActionsRow {
                SavioGhostButton(
                    text = "Itinéraire",
                    icon = Icons.Filled.Directions,
                    onClick = {
                        val address = addressLine.trim()
                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                        uriHandler.openUri(uri.toString())
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            SavioRowLine()
        }
        if (isEditing) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = onAddressLine2Change,
                    label = { Text("Complément d'adresse") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = floor,
                        onValueChange = onFloorChange,
                        label = { Text("Étage") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = doorCode,
                        onValueChange = onDoorCodeChange,
                        label = { Text("Code accès") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            }
            SavioRowLine()
        }
        if (isEditing) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Téléphone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let { err ->
                            Text(text = err, color = SavioUi.DestructiveRed, fontSize = 12.sp)
                        }
                    },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            errorBorderColor = SavioUi.DestructiveRed,
                            errorLabelColor = SavioUi.DestructiveRed,
                            errorSupportingTextColor = SavioUi.DestructiveRed,
                        ),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                )
            }
        } else {
            if (phone.isNotBlank()) {
                SavioInfoLinkRow(
                    icon = Icons.Filled.Phone,
                    value = phone,
                    onClick = { uriHandler.openUri("tel:${phone.trim()}") },
                )
                SavioRowLine()
            }
            if (email.isNotBlank()) {
                SavioInfoLinkRow(
                    icon = Icons.Filled.Email,
                    value = email,
                    onClick = { uriHandler.openUri("mailto:${email.trim()}") },
                )
            }
            if (notes.isNotBlank()) {
                if (phone.isBlank() && email.isBlank()) {
                    // no divider needed
                } else {
                    SavioRowLine()
                }
                Text(
                    text = notes,
                    fontSize = 13.sp,
                    color = SavioRefonte.Muted,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
                )
            }
        }
    }
}
