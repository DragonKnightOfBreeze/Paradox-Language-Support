package icu.windea.pls.core.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.observable.properties.*
import icu.windea.pls.lang.model.*

object PlsDataKeys {
	val gameTypeKey = DataKey.create<ParadoxGameType>("PARADOX_GAME_TYPE")
	val gameTypePropertyKey = DataKey.create<GraphProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY")
}
