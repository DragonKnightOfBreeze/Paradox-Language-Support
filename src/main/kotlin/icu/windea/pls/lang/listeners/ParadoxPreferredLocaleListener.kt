package icu.windea.pls.lang.listeners

import com.intellij.util.messages.Topic

/**
 * 监听首选语言环境的更改。
 */
interface ParadoxPreferredLocaleListener {
    fun onChange(oldLocale: String, newLocale: String)

    companion object {
        val TOPIC = Topic(ParadoxPreferredLocaleListener::class.java, Topic.BroadcastDirection.NONE)
    }
}

