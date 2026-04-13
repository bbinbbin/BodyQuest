package com.bodyquest.wear.data

import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneConnectionRepository @Inject constructor(
    private val nodeClient: NodeClient,
    private val messageClient: MessageClient
) {
    companion object {
        const val PING_PATH = "/bodyquest/ping"
    }

    private val _connectedNodeId = MutableStateFlow<String?>(null)
    val connectedNodeId: StateFlow<String?> = _connectedNodeId

    suspend fun checkConnection(): Boolean {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            val phone = nodes.firstOrNull()
            _connectedNodeId.value = phone?.id
            phone != null
        } catch (e: Exception) {
            _connectedNodeId.value = null
            false
        }
    }

    suspend fun sendPingToPhone(): Boolean {
        val nodeId = _connectedNodeId.value ?: return false
        return try {
            messageClient.sendMessage(nodeId, PING_PATH, "ping".toByteArray()).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
