package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.extension.diagram.provider.*

abstract class ParadoxDiagramDataModel(
    project: Project,
    val provider: ParadoxDiagramProvider
): DiagramDataModel<PsiElement>(project, provider), ModificationTracker  {
    val gameType get() = provider.gameType
}