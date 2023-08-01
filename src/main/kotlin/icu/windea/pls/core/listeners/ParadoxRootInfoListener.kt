package icu.windea.pls.core.listeners

import com.intellij.util.messages.*
import icu.windea.pls.model.*

/**
 * 监听根信息的添加和移除。
 */
interface ParadoxRootInfoListener {
    fun onAdd(rootInfo: ParadoxRootInfo)
    
    fun onRemove(rootInfo: ParadoxRootInfo)
    
    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxRootInfoListener::class.java, Topic.BroadcastDirection.NONE, true)
    }
}