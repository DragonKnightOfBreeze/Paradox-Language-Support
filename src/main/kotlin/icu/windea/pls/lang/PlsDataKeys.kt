package icu.windea.pls.lang

import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.observable.properties.GraphProperty
import icu.windea.pls.model.ParadoxGameType

object PlsDataKeys {
    val gameType by lazy { DataKey.create<ParadoxGameType>("PARADOX_GAME_TYPE") }
    val gameTypeProperty by lazy { DataKey.create<GraphProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY") }
}
