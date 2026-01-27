package icu.windea.pls.config.listeners

import com.intellij.util.messages.Topic

/**
 * 监听本地规则目录的更改。
 */
interface CwtConfigDirectoriesListener {
    fun onChange()

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(CwtConfigDirectoriesListener::class.java, Topic.BroadcastDirection.NONE)
    }
}

