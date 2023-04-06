package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*

abstract class ParadoxDiagramSettings<T: BaseState>(initialState: T) : SimplePersistentStateComponent<T>(initialState)