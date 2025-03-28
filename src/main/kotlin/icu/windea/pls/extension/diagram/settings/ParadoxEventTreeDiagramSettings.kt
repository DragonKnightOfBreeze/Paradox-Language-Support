package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.project.*

abstract class ParadoxEventTreeDiagramSettings<T : ParadoxDiagramSettings.State>(
    project: Project,
    initialState: T
) : ParadoxDiagramSettings<T>(project, initialState)
