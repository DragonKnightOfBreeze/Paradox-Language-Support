package icu.windea.pls.ai.model.requests

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint

abstract class ManipulateLocalisationAiRequest(
    val project: Project,
    val file: PsiFile,
    val localisationContexts: List<ParadoxLocalisationContext>
) : PromptVariablesAwareAiRequest {
    @Volatile
    var index: Int = 0

    override fun toPromptVariables(variables: MutableMap<String, Any?>): Map<String, Any?> {
        variables.put("index", index)
        variables.put("total", localisationContexts.size)

        val gameType = selectGameType(file) ?: PlsFacade.getSettings().defaultGameType
        val fileInfo: ParadoxFileInfo? by lazy { selectFile(file)?.fileInfo }
        variables["game_type_id"] = gameType.id
        variables["game_type_title"] = gameType.title
        variables["mod_name"] = fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.Mod>()?.name
        variables["file_path"] = fileInfo?.path?.path

        variables["supports_concept_command"] = ParadoxSyntaxConstraint.LocalisationConceptCommand.supports(file)
        variables["supports_text_format"] = ParadoxSyntaxConstraint.LocalisationTextFormat.supports(file)
        variables["supports_text_icon"] = ParadoxSyntaxConstraint.LocalisationTextIcon.supports(file)

        return variables
    }
}

