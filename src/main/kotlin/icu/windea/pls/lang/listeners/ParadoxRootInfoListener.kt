package icu.windea.pls.lang.listeners

import com.intellij.util.messages.Topic
import icu.windea.pls.model.ParadoxRootInfo

/**
 * 监听根信息的添加。
 */
interface ParadoxRootInfoListener {
    fun onAdd(rootInfo: ParadoxRootInfo)

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxRootInfoListener::class.java, Topic.BroadcastDirection.NONE, true)
    }
}
