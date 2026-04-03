package com.bodyquest.app.ui.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestDetailViewModel @Inject constructor(
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<QuestEntity>>(UiState.Loading)
    val uiState: StateFlow<UiState<QuestEntity>> = _uiState

    fun loadQuest(questId: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val quest = questRepository.getQuestById(questId)
                if (quest != null) {
                    _uiState.value = UiState.Success(quest)
                } else {
                    _uiState.value = UiState.Error("퀘스트를 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "퀘스트를 불러올 수 없습니다.")
            }
        }
    }
}
