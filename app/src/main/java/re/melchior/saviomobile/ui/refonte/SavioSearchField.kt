package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioRefonte

@Composable
fun SavioSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Rechercher un client",
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 14.dp),
        shape = RoundedCornerShape(14.dp),
        color = androidx.compose.ui.graphics.Color.White,
        border = BorderStroke(1.dp, SavioRefonte.Line),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color(0xFF95A0AE),
                modifier = Modifier.size(20.dp),
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                textStyle =
                    TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = SavioRefonte.Ink,
                    ),
                singleLine = true,
                cursorBrush = SolidColor(SavioRefonte.Navy),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = SavioRefonte.Muted,
                        )
                    }
                    inner()
                },
            )
            if (query.isNotEmpty()) {
                Text(
                    text = "×",
                    fontSize = 24.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF95A0AE),
                    modifier = Modifier.clickable { onQueryChange("") },
                )
            }
        }
    }
}
