package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo

data class ParadoxSyntaxInspectionContext(
    val tool: LocalInspectionTool,
    val holder: ProblemsHolder,
) {
    val file: VirtualFile? = selectFile(holder.file)
    val rootFile: VirtualFile? = selectRootFile(holder.file)
    val fileInfo: ParadoxFileInfo? = file?.fileInfo
    val rootInfo: ParadoxRootInfo? = fileInfo?.rootInfo
    val gameType: ParadoxGameType? = rootInfo?.gameType
    val gameVersion: String? = rootInfo?.gameVersion
}
