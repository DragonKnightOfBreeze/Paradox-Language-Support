package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import icu.windea.pls.extension.diagram.provider.*

abstract class ParadoxDiagramDataModel(
    project: Project,
    val file: VirtualFile?,
    val provider: ParadoxDiagramProvider,
): DiagramDataModel<PsiElement>(project, provider), ModificationTracker  {
    val gameType get() = provider.gameType
    
    val originalFile get() = file?.getUserData(DiagramDataKeys.ORIGINAL_ELEMENT) as? VirtualFile?
}