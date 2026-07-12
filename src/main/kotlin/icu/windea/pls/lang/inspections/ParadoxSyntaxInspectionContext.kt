package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

data class ParadoxSyntaxInspectionContext(
    val tool: LocalInspectionTool,
    val holder: ProblemsHolder,
    val file: VirtualFile?,
    val rootFile: VirtualFile?,
    val gameType: ParadoxGameType?,
    val gameVersion: String?,
) {
    val project: Project get() = holder.project
}

