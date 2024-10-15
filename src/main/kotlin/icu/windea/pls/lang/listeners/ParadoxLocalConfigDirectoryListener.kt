package icu.windea.pls.lang.listeners

import com.intellij.util.messages.*

/**
 * 监听本地规则目录的更改。
 */
interface ParadoxLocalConfigDirectoryListener {
    fun onChange(oldDirectory: String, newDirectory: String)

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxLocalConfigDirectoryListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
