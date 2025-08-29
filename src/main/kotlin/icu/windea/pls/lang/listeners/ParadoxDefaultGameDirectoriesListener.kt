package icu.windea.pls.lang.listeners

import com.intellij.util.messages.Topic

/**
 * 监听默认游戏目录映射的更改。
 */
interface ParadoxDefaultGameDirectoriesListener {
    fun onChange(oldGameDirectories: Map<String, String>, newGameDirectories: Map<String, String>)

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxDefaultGameDirectoriesListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
