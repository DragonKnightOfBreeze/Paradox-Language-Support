package icu.windea.pls.lang.listeners

import com.intellij.util.messages.Topic

/**
 * 监听本地规则目录的更改。
 */
interface ParadoxConfigDirectoriesListener {
    fun onChange()

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxConfigDirectoriesListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
