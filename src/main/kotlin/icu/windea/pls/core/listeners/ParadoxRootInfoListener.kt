package icu.windea.pls.core.listeners

import com.intellij.util.messages.*
import icu.windea.pls.lang.model.*

/**
 * 监听根信息的添加和移除。
 */
interface ParadoxRootInfoListener {
    fun onAdd(rootInfo: ParadoxRootInfo)
    
    fun onRemove(rootInfo: ParadoxRootInfo)
    
    companion object {
        @Topic.ProjectLevel
        val TOPIC = Topic.create("ParadoxRootInfoListener", ParadoxRootInfoListener::class.java)
    }
}