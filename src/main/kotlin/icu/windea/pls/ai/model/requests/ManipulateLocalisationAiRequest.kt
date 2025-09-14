package icu.windea.pls.ai.model.requests

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint

abstract class ManipulateLocalisationAiRequest(
    val project: Project,
    val file: PsiFile,
    val localisationContexts: List<ParadoxLocalisationContext>
) : AiRequest {
    @Volatile
    var index: Int = 0

    val context = Context(this)
    val predicates = Predicates(this)

    class Context(request: ManipulateLocalisationAiRequest) {
        private val fileInfo: ParadoxFileInfo? by lazy { selectFile(request.file)?.fileInfo }

        fun isEmpty() = fileInfo == null

        val gameType: ParadoxGameType? by lazy { fileInfo?.rootInfo?.gameType }
        val filePath: String? by lazy { fileInfo?.path?.path }
        val fileName: String? by lazy { fileInfo?.path?.fileName }
        val modName: String? by lazy { fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.Mod>()?.name }
    }

    class Predicates(request: ManipulateLocalisationAiRequest) {
        val supportsConceptCommand = ParadoxSyntaxConstraint.LocalisationConceptCommand.supports(request.file)
        val supportsTextIcon = ParadoxSyntaxConstraint.LocalisationTextIcon.supports(request.file)
        val supportsTextFormat = ParadoxSyntaxConstraint.LocalisationTextFormat.supports(request.file)
    }

    override fun toPromptVariables(variables: MutableMap<String, Any?>): Map<String, Any?> {
        variables.put("index", index)
        variables.put("total", localisationContexts.size)

        val fileInfo: ParadoxFileInfo? by lazy { selectFile(file)?.fileInfo }
        val gameType = fileInfo?.rootInfo?.gameType
        variables["game_type_id"] = gameType?.id
        variables["game_type_title"] = gameType?.title
        variables["file_path"] = fileInfo?.path?.path
        variables["file_name"] = fileInfo?.path?.fileName
        variables["mod_name"] = fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.Mod>()?.name

        variables["supports_concept_command"] = ParadoxSyntaxConstraint.LocalisationConceptCommand.supports(file)
        variables["supports_text_icon"] = ParadoxSyntaxConstraint.LocalisationTextIcon.supports(file)
        variables["supports_text_format"] = ParadoxSyntaxConstraint.LocalisationTextFormat.supports(file)

        return variables
    }
}

