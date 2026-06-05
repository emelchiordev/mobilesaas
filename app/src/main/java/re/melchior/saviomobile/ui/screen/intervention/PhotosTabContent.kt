package re.melchior.saviomobile.ui.screen.intervention

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.ui.component.PhotoGrid
import re.melchior.saviomobile.ui.component.SavioEmptyState
import re.melchior.saviomobile.ui.refonte.SavioPhotosListHeader
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi
import re.melchior.saviomobile.ui.viewmodel.PhotoViewModel

@Composable
fun PhotosTabContent(
    interventionId: String,
    unitId: String,
    customerId: String,
    onOpenCamera: (unitId: String, customerId: String) -> Unit,
    isOnline: Boolean = true,
    viewModel: PhotoViewModel = hiltViewModel(),
) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val refonte = useSavioRefonteUi()

    LaunchedEffect(interventionId) {
        viewModel.loadPhotos(interventionId)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(if (refonte) 15.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (photos.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                SavioEmptyState(
                    icon = Icons.Outlined.PhotoCamera,
                    title = "Aucune photo ajoutée",
                    subtitle = "Ajoutez des photos de l'intervention.",
                    primaryActionLabel = "+ Ajouter une photo",
                    primaryEnabled = isOnline,
                    onPrimaryAction = { onOpenCamera(unitId, customerId) },
                )
                if (!isOnline) {
                    Text(
                        text = "Disponible en ligne uniquement",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        } else {
            if (refonte) {
                SavioPhotosListHeader(
                    count = photos.size,
                    onAddClick = { onOpenCamera(unitId, customerId) },
                )
            }
            PhotoGrid(
                photos = photos,
                onAddPhotoClick = { onOpenCamera(unitId, customerId) },
                onPhotoClick = { photo ->
                    viewModel.selectPhoto(photo)
                },
                onDeletePhoto = { photo ->
                    viewModel.deletePhoto(photo)
                },
                canAddPhoto = isOnline,
            )
        }
    }
}
