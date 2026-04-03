package com.bodyquest.app.ui.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestTreeState(
    val category: String = "",
    val bodyParts: List<String> = emptyList(),
    val selectedBodyPart: String? = null,
    val quests: List<QuestEntity> = emptyList(),
    val treeLevel: TreeLevel = TreeLevel.BODY_PART
)

enum class TreeLevel {
    BODY_PART,
    QUEST_LIST
}

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<QuestTreeState>>(UiState.Loading)
    val uiState: StateFlow<UiState<QuestTreeState>> = _uiState

    fun loadCategory(category: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val parts = questRepository.getBodyParts(category)
                _uiState.value = UiState.Success(
                    QuestTreeState(
                        category = category,
                        bodyParts = parts,
                        treeLevel = TreeLevel.BODY_PART
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "퀘스트를 불러올 수 없습니다.")
            }
        }
    }

    fun selectBodyPart(bodyPart: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(
            current.copy(
                selectedBodyPart = bodyPart,
                treeLevel = TreeLevel.QUEST_LIST
            )
        )
        viewModelScope.launch {
            try {
                questRepository.getQuestsByBodyPart(current.category, bodyPart)
                    .collectLatest { quests ->
                        val state = (_uiState.value as? UiState.Success)?.data ?: return@collectLatest
                        _uiState.value = UiState.Success(state.copy(quests = quests))
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "퀘스트를 불러올 수 없습니다.")
            }
        }
    }

    fun goBackToBodyParts() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(
            current.copy(
                selectedBodyPart = null,
                quests = emptyList(),
                treeLevel = TreeLevel.BODY_PART
            )
        )
    }

    fun retry() {
        val current = (_uiState.value as? UiState.Success)?.data
        if (current != null) {
            loadCategory(current.category)
        }
    }
}
