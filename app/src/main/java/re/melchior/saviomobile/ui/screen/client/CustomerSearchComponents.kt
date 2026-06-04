package re.melchior.saviomobile.ui.screen.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.data.remote.dto.CustomerSearchRowDto
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.savioFieldColors

@Composable
fun CustomerSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Rechercher un client",
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = savioFieldColors(),
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
fun CustomerSearchResultsSection(
    query: String,
    results: List<CustomerSearchRowDto>,
    isLoading: Boolean,
    error: String?,
    onSelect: (CustomerSearchRowDto) -> Unit,
    emptyHint: String = "Saisissez un nom, un téléphone ou un e-mail.",
) {
    if (query.isBlank()) {
        Text(
            text = emptyHint,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
        return
    }
    if (isLoading) {
        Spacer(modifier = Modifier.height(12.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = SavioPalette.Accent,
            strokeWidth = 2.dp,
        )
    }
    error?.let { err ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    }
    results.forEach { row ->
        Spacer(modifier = Modifier.height(8.dp))
        CustomerSearchResultCard(row = row, onClick = { onSelect(row) })
    }
}

@Composable
fun CustomerSearchResultCard(
    row: CustomerSearchRowDto,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = row.resolvedDisplayName(),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = row.formattedAddress(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            row.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = phone,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
