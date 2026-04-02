package re.melchior.saviomobile.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.entity.PhotoEntity
import re.melchior.saviomobile.data.repository.PhotoRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Photos de l'intervention courante
    private val _photos = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val photos: StateFlow<List<PhotoEntity>> = _photos.asStateFlow()

    // Etat UI
    private val _uiState = MutableStateFlow<PhotoUiState>(PhotoUiState.Idle)
    val uiState: StateFlow<PhotoUiState> = _uiState.asStateFlow()

    // Nombre de photos non synced
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    // Photo sélectionnée pour affichage plein écran
    private val _selectedPhoto = MutableStateFlow<PhotoEntity?>(null)
    val selectedPhoto: StateFlow<PhotoEntity?> = _selectedPhoto.asStateFlow()

    /**
     * Charge les photos d'une intervention — appelé depuis InterventionActive
     */
    fun loadPhotos(interventionId: String) {
        photoRepository.getPhotosForIntervention(interventionId)
            .onEach { _photos.value = it }
            .launchIn(viewModelScope)

        photoRepository.getPendingCount()
            .onEach { _pendingCount.value = it }
            .launchIn(viewModelScope)
    }

    /**
     * Sauvegarde une photo prise par CameraX
     */
    fun savePhoto(
        interventionId: String,
        unitId: String,
        customerId: String,
        sourceFile: File
    ) {
        viewModelScope.launch {
            _uiState.value = PhotoUiState.Saving
            try {
                photoRepository.savePhoto(
                    interventionId = interventionId,
                    unitId = unitId,
                    customerId = customerId,
                    sourceFile = sourceFile
                )
                _uiState.value = PhotoUiState.Idle
            } catch (e: Exception) {
                _uiState.value = PhotoUiState.Error(e.message ?: "Erreur lors de la sauvegarde")
            }
        }
    }

    /**
     * Supprime une photo
     */
    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            _uiState.value = PhotoUiState.Deleting
            try {
                photoRepository.deletePhoto(photo)
                _uiState.value = PhotoUiState.Idle
                if (_selectedPhoto.value?.id == photo.id) {
                    _selectedPhoto.value = null
                }
            } catch (e: Exception) {
                _uiState.value = PhotoUiState.Error(e.message ?: "Erreur lors de la suppression")
            }
        }
    }

    /**
     * Sélectionne une photo pour affichage plein écran
     */
    fun selectPhoto(photo: PhotoEntity?) {
        _selectedPhoto.value = photo
    }

    /**
     * Réinitialise l'état UI après affichage du message d'erreur
     */
    fun clearUiState() {
        _uiState.value = PhotoUiState.Idle
    }
}

sealed class PhotoUiState {
    object Idle : PhotoUiState()
    object Saving : PhotoUiState()
    object Deleting : PhotoUiState()
    data class Error(val message: String) : PhotoUiState()
}