package icu.windea.pls.lang

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.observable.properties.*
import icu.windea.pls.model.*

object PlsDataKeys {
    val gameType by lazy { DataKey.create<ParadoxGameType>("PARADOX_GAME_TYPE") }
    val gameTypeProperty by lazy { DataKey.create<GraphProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY") }
}
