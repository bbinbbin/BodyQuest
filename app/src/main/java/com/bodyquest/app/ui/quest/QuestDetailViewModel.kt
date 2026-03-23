package com.bodyquest.app.ui.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.repository.QuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestDetailViewModel @Inject constructor(
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _quest = MutableStateFlow<QuestEntity?>(null)
    val quest: StateFlow<QuestEntity?> = _quest

    fun loadQuest(questId: String) {
        viewModelScope.launch {
            _quest.value = questRepository.getQuestById(questId)
        }
    }
}
