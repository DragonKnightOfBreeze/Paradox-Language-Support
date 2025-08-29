package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.matchesPath
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.properties

class NonTriggeredEventInspection : LocalInspectionTool() {
    //see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/88

    override fun isAvailableForFile(file: PsiFile): Boolean {
        //仅检查事件脚本文件
        if (file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path
        return "txt" == filePath.fileExtension && "events".matchesPath(filePath.path)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)

        file.properties().options(inline = true).forEach f@{ element ->
            val definitionInfo = element.definitionInfo ?: return@f
            if (definitionInfo.type != ParadoxDefinitionTypes.Event) return@f
            if ("triggered" !in definitionInfo.typeConfig.subtypes.keys) return@f  //no "triggered" subtype declared, skip
            if ("inherited" in definitionInfo.subtypes) return@f  //ignore inherited events
            if ("triggered" in definitionInfo.subtypes) return@f
            val fixes = buildList {
                if (element.block != null) this += Fix1(element)
            }.toTypedArray()
            holder.registerProblem(element.propertyKey, PlsBundle.message("inspection.script.nonTriggeredEvent.desc"), *fixes)
        }

        return holder.resultsArray
    }

    private class Fix1(
        element: PsiElement
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
        //add "is_triggered_only = yes" into declaration (after "id" field or at start)

        override fun getText() = PlsBundle.message("inspection.script.nonTriggeredEvent.fix.1")

        override fun getFamilyName() = text

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val element = startElement.castOrNull<ParadoxScriptProperty>() ?: return
            val definitionInfo = element.definitionInfo ?: return
            val block = element.block ?: return
            val nameField = definitionInfo.typeConfig.nameField
            val insertAfterElement = if (nameField == null) null else element.findProperty(nameField)
            val textToInsert = "is_triggered_only = yes"
            block.addAfter(ParadoxScriptElementFactory.createPropertyFromText(project, textToInsert), insertAfterElement)
            block.addAfter(ParadoxScriptElementFactory.createLine(project), insertAfterElement)
        }
    }
}
