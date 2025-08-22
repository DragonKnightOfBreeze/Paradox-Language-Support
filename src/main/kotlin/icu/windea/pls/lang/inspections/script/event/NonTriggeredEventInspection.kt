package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.dataFlow.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

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
