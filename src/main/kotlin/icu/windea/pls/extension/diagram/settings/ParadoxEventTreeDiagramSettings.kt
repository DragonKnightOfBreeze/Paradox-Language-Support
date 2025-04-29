package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.project.*
import icu.windea.pls.model.*

abstract class ParadoxEventTreeDiagramSettings<T : ParadoxDiagramSettings.State>(
    project: Project,
    initialState: T,
    gameType: ParadoxGameType
) : ParadoxDiagramSettings<T>(project, initialState, gameType)
