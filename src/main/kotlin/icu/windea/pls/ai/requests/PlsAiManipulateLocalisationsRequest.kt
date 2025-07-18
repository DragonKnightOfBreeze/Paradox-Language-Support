package icu.windea.pls.ai.requests

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.model.*

abstract class PlsAiManipulateLocalisationsRequest(
    val project: Project,
    val file: PsiFile?,
    val localisationContexts: List<ParadoxLocalisationContext>,
    description: String?,
): PlsAiRequest {
    val description: String? = description?.orNull()?.substringBefore('\n')?.trim() //去除首尾空白，且截断换行符之后的文本

    var index: Int = 0

    val fileInfo: ParadoxFileInfo? = selectFile(file ?: localisationContexts.firstOrNull()?.element)?.fileInfo

    val gameType: ParadoxGameType = fileInfo?.rootInfo?.gameType.orDefault()
    val filePath: String? = fileInfo?.path?.path
    val fileName: String? = fileInfo?.path?.fileName
    val modName: String? = fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.Mod>()?.name
}
