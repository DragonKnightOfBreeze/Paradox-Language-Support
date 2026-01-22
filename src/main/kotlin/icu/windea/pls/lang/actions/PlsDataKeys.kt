package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.observable.properties.ObservableProperty
import icu.windea.pls.model.ParadoxGameType

object PlsDataKeys {
    val gameTypeProperty = DataKey.create<ObservableProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY")
}
