package com.bodyquest.app.ui.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.repository.QuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

class QuestViewModel(
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _treeState = MutableStateFlow(QuestTreeState())
    val treeState: StateFlow<QuestTreeState> = _treeState

    fun loadCategory(category: String) {
        _treeState.value = QuestTreeState(category = category, treeLevel = TreeLevel.BODY_PART)
        viewModelScope.launch {
            val parts = questRepository.getBodyParts(category)
            _treeState.value = _treeState.value.copy(bodyParts = parts)
        }
    }

    fun selectBodyPart(bodyPart: String) {
        _treeState.value = _treeState.value.copy(
            selectedBodyPart = bodyPart,
            treeLevel = TreeLevel.QUEST_LIST
        )
        viewModelScope.launch {
            questRepository.getQuestsByBodyPart(_treeState.value.category, bodyPart)
                .collectLatest { quests ->
                    _treeState.value = _treeState.value.copy(quests = quests)
                }
        }
    }

    fun goBackToBodyParts() {
        _treeState.value = _treeState.value.copy(
            selectedBodyPart = null,
            quests = emptyList(),
            treeLevel = TreeLevel.BODY_PART
        )
    }

    class Factory(private val questRepository: QuestRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuestViewModel(questRepository) as T
        }
    }
}
