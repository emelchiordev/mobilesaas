package re.savio.mobile.ui.refonte

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
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
import re.savio.mobile.ui.theme.ViolettTokens

@Composable
fun SavioSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Rechercher un client",
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = ViolettTokens.Faint,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = ViolettTokens.Muted,
                modifier = Modifier.size(20.dp),
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f).padding(horizontal = 11.dp),
                textStyle =
                    TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = ViolettTokens.Ink,
                    ),
                singleLine = true,
                cursorBrush = SolidColor(ViolettTokens.Accent),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = ViolettTokens.Muted,
                        )
                    }
                    inner()
                },
            )
            if (query.isNotEmpty()) {
                Text(
                    text = "×",
                    fontSize = 22.sp,
                    color = ViolettTokens.Muted,
                    modifier = Modifier.clickable { onQueryChange("") }.padding(start = 4.dp),
                )
            }
        }
    }
}
