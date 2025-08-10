package icu.windea.pls.ai.requests

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.model.*

abstract class ManipulateLocalisationAiRequest(
    val project: Project,
    val file: PsiFile,
    val localisationContexts: List<ParadoxLocalisationContext>
): AiRequest {
    @Volatile
    var index: Int = 0

    val context = Context(this)

    class Context(request: ManipulateLocalisationAiRequest) {
        private val fileInfo: ParadoxFileInfo? by lazy { selectFile(request.file)?.fileInfo }

        val gameType: ParadoxGameType by lazy { fileInfo?.rootInfo?.gameType.orDefault() }
        val filePath: String? by lazy { fileInfo?.path?.path }
        val fileName: String? by lazy { fileInfo?.path?.fileName }
        val modName: String? by lazy { fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.Mod>()?.name }
    }
}

