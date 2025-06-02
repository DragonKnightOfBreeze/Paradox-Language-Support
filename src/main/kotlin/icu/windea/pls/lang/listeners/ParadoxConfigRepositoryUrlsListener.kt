package icu.windea.pls.lang.listeners

import com.intellij.util.messages.Topic

/**
 * 监听规则仓库地址的更改。
 */
interface ParadoxConfigRepositoryUrlsListener {
    fun onChange()

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxConfigRepositoryUrlsListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
