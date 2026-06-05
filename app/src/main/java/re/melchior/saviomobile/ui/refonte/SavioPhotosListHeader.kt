package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioRefonte

@Composable
fun SavioPhotosListHeader(
    count: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SavioInfoIconBox(icon = Icons.Filled.CameraAlt, size = 34.dp)
        Text(
            text = "Photos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SavioRefonte.Ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = count.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SavioRefonte.Navy,
            modifier =
                Modifier
                    .background(SavioRefonte.Tint, RoundedCornerShape(999.dp))
                    .padding(horizontal = 11.dp, vertical = 2.dp),
        )
        IconButton(
            onClick = onAddClick,
            modifier =
                Modifier
                    .size(38.dp)
                    .background(Color.White, RoundedCornerShape(11.dp))
                    .border(1.dp, SavioRefonte.Line, RoundedCornerShape(11.dp)),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Ajouter une photo",
                tint = SavioRefonte.Navy,
            )
        }
    }
}
