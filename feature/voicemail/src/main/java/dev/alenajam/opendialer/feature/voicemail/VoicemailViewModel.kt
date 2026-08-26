package dev.alenajam.opendialer.feature.voicemail

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementRepository
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import dev.alenajam.opendialer.data.voicemail.Voicemail
import dev.alenajam.opendialer.data.voicemail.VoicemailRepository
import dev.alenajam.opendialer.data.voicemail.VoicemailRepositoryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoicemailViewModel @Inject constructor(
    private val repository: VoicemailRepository,
    private val app: Application,
    private val callPlacementRepository: CallPlacementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<VoicemailUiState>(VoicemailUiState.Loading)
    val uiState: StateFlow<VoicemailUiState> = _uiState.asStateFlow()

    private val _hasRuntimePermission = MutableStateFlow(PermissionUtils.hasMakeCallPermission(app))
    val hasRuntimePermission: StateFlow<Boolean> = _hasRuntimePermission.asStateFlow()

    private val _playingId = MutableStateFlow<Long?>(null)
    val playingId: StateFlow<Long?> = _playingId.asStateFlow()

    private var observeJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        refresh()
    }

    fun refresh() {
        if (!_hasRuntimePermission.value) return
        observeJob?.cancel()
        _uiState.value = VoicemailUiState.Loading
        observeJob = viewModelScope.launch {
            repository.getVoicemails().collect { state ->
                _uiState.value = when (state) {
                    VoicemailRepositoryState.Unavailable -> VoicemailUiState.Unavailable
                    is VoicemailRepositoryState.Available -> VoicemailUiState.Available(state.voicemails)
                }
            }
        }
    }

    fun handleRuntimePermissionGranted() {
        _hasRuntimePermission.value = PermissionUtils.hasMakeCallPermission(app)
        if (_hasRuntimePermission.value) refresh()
    }

    fun callVoicemail(): CallPlacementResult = callPlacementRepository.placeVoicemailCall()

    fun callVoicemail(account: CallAccount): CallPlacementResult =
        callPlacementRepository.placeVoicemailCall(account)

    fun play(voicemail: Voicemail) {
        if (!voicemail.hasContent) {
            viewModelScope.launch(Dispatchers.IO) { repository.requestContent(voicemail) }
            return
        }

        mediaPlayer?.release()
        _playingId.value = null
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(app, voicemail.uri)
                setOnPreparedListener { player ->
                    _playingId.value = voicemail.id
                    player.start()
                }
                setOnCompletionListener { player ->
                    _playingId.value = null
                    player.release()
                    mediaPlayer = null
                }
                setOnErrorListener { player, _, _ ->
                    _playingId.value = null
                    player.release()
                    mediaPlayer = null
                    true
                }
                prepareAsync()
            }
            viewModelScope.launch(Dispatchers.IO) { repository.markRead(voicemail) }
        } catch (_: Exception) {
            mediaPlayer?.release()
            mediaPlayer = null
            _playingId.value = null
        }
    }

    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }
}
