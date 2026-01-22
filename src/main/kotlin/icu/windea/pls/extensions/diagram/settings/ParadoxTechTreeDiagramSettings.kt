package icu.windea.pls.extensions.diagram.settings

import com.intellij.openapi.project.Project
import icu.windea.pls.model.ParadoxGameType

abstract class ParadoxTechTreeDiagramSettings<T : ParadoxDiagramSettings.State>(
    project: Project,
    initialState: T,
    override val gameType: ParadoxGameType
) : ParadoxDiagramSettings<T>(project, initialState, gameType)
