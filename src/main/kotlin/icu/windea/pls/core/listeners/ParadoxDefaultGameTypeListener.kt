package icu.windea.pls.core.listeners

import com.intellij.openapi.project.*
import com.intellij.util.messages.*
import icu.windea.pls.lang.model.*

/**
 * 监听默认游戏类型的更改。
 */
interface ParadoxDefaultGameTypeListener {
    fun onChange(gameType: ParadoxGameType)
    
    companion object {
        @Topic.AppLevel
        val TOPIC = Topic.create("ParadoxDefaultGameTypeListener", ParadoxDefaultGameTypeListener::class.java)
    }
}