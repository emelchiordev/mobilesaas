package re.savio.mobile.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import re.savio.mobile.data.local.entity.PhotoEntity
import re.savio.mobile.ui.theme.SavioPalette
import java.io.File

@Composable
fun PhotoGrid(
    photos: List<PhotoEntity>,
    onAddPhotoClick: () -> Unit,
    onPhotoClick: (PhotoEntity) -> Unit,
    onDeletePhoto: (PhotoEntity) -> Unit,
    modifier: Modifier = Modifier,
    canAddPhoto: Boolean = true
) {
    var photoToDelete by remember { mutableStateOf<PhotoEntity?>(null) }
    var selectedPhoto by remember { mutableStateOf<PhotoEntity?>(null) } // ← ajouté

    // Viewer plein écran
    selectedPhoto?.let { photo ->
        PhotoViewerDialog(
            photo = photo,
            onDismiss = { selectedPhoto = null }
        )
    }

    // Dialog confirmation suppression
    photoToDelete?.let { photo ->
        DeletePhotoDialog(
            onConfirm = {
                onDeletePhoto(photo)
                photoToDelete = null
            },
            onDismiss = { photoToDelete = null }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        if (canAddPhoto) {
            item { AddPhotoButton(onClick = onAddPhotoClick) }
        }

        items(photos, key = { it.id }) { photo ->
            PhotoItem(
                photo = photo,
                onClick = { selectedPhoto = photo }, // ← ouvre le viewer
                onDeleteClick = { photoToDelete = photo }
            )
        }
    }
}

@Composable
private fun PhotoItem(
    photo: PhotoEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        // Image — locale si pas encore synced, sinon distante
        AsyncImage(
            model = if (photo.syncStatus == "SYNCED" && photo.remoteUrl != null) {
                photo.remoteUrl
            } else {
                File(photo.localPath)
            },
            contentDescription = "Photo intervention",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Badge sync status
        SyncBadge(
            syncStatus = photo.syncStatus,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
        )

        // Bouton suppression
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Supprimer",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Ajouter une photo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun SyncBadge(
    syncStatus: String,
    modifier: Modifier = Modifier
) {
    when (syncStatus) {
        "PENDING" -> Box(
            modifier = modifier
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "En attente de sync",
                tint = Color.Yellow,
                modifier = Modifier.size(12.dp)
            )
        }
        "ERROR" -> Box(
            modifier = modifier
                .background(
                    color = Color.Red.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SyncProblem,
                contentDescription = "Erreur sync",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        // SYNCED → pas de badge
    }
}

@Composable
private fun DeletePhotoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer la photo ?") },
        text = { Text("Cette action est irréversible.") },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text("Supprimer", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}