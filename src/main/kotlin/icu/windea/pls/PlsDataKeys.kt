package icu.windea.pls

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.observable.properties.*
import icu.windea.pls.model.*

object PlsDataKeys

val PlsDataKeys.gameType by lazy { DataKey.create<ParadoxGameType>("PARADOX_GAME_TYPE") }
val PlsDataKeys.gameTypeProperty by lazy { DataKey.create<GraphProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY") }
