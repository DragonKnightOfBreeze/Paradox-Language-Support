package icu.windea.pls.config.listeners

import com.intellij.util.messages.Topic

/**
 * 监听规则仓库地址的更改。
 */
interface CwtConfigRepositoryUrlsListener {
    fun onChange()

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(CwtConfigRepositoryUrlsListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
