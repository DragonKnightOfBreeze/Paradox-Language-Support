package icu.windea.pls

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.observable.properties.*
import icu.windea.pls.lang.model.*

object PlsDataKeys

val PlsDataKeys.gameTypeKey by lazy { DataKey.create<ParadoxGameType>("PARADOX_GAME_TYPE") }
val PlsDataKeys.gameTypePropertyKey by lazy { DataKey.create<GraphProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY") }