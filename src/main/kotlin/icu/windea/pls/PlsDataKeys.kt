package icu.windea.pls

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.observable.properties.*
import icu.windea.pls.model.*

object PlsDataKeys {
	val gameTypePropertyKey = DataKey.create<GraphProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY")
	val rootTypePropertyKey = DataKey.create<GraphProperty<ParadoxRootType>>("PARADOX_ROOT_TYPE_PROPERTY")
}