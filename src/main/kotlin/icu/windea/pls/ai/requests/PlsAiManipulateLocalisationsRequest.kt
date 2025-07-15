package icu.windea.pls.ai.requests

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.orDefault

abstract class PlsAiManipulateLocalisationsRequest(
    val project: Project,
    val file: PsiFile?,
    val inputContexts: List<ParadoxLocalisationContext>,
    inputText: String,
    inputDescription: String?,
): PlsAiRequest {
    val text: String = inputText.trim() //去除首尾空白
    val description: String? = inputDescription?.orNull()?.substringBefore('\n')?.trim() //去除首尾空白，且截断换行符之后的文本

    var index: Int = 0

    val fileInfo: ParadoxFileInfo? = selectFile(file ?: inputContexts.firstOrNull()?.element)?.fileInfo

    val gameType: ParadoxGameType = fileInfo?.rootInfo?.gameType.orDefault()
    val filePath: String? = fileInfo?.path?.path
    val fileName: String? = fileInfo?.path?.fileName
    val modName: String? = fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.Mod>()?.name
}
