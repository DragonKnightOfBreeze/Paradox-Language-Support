package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

class NonTriggeredEventInspection : EventInspectionBase() {
    // see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/88

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)

        val elements = file.properties(inline = true)
        for (element in elements) {
            ProgressManager.checkCanceled()
            val definitionInfo = element.definitionInfo ?: continue
            if (definitionInfo.type != ParadoxDefinitionTypes.event) continue
            if ("triggered" !in definitionInfo.typeConfig.subtypes.keys) continue  // no `triggered` subtype declared, skip
            if ("inherited" in definitionInfo.subtypes) continue  // ignore inherited events
            if ("triggered" in definitionInfo.subtypes) continue
            val description = PlsBundle.message("inspection.script.nonTriggeredEvent.desc")
            val fixes = getFixes(element)
            holder.registerProblem(element.propertyKey, description, *fixes)
        }

        return holder.resultsArray
    }

    private fun getFixes(element: ParadoxScriptProperty): Array<LocalQuickFix> {
        return buildList {
            if (element.block != null) this += (Fix1(element))
        }.toTypedArray()
    }

    private class Fix1(
        element: PsiElement
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
        // add `is_triggered_only = yes` into declaration (after `id` field or at start)

        override fun getText() = PlsBundle.message("inspection.script.nonTriggeredEvent.fix.1.name")

        override fun getFamilyName() = text

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val element = startElement.castOrNull<ParadoxScriptProperty>() ?: return
            val definitionInfo = element.definitionInfo ?: return
            val block = element.block ?: return
            val nameField = definitionInfo.typeConfig.nameField
            val insertAfterElement = if (nameField == null) null else selectScope { element.properties().ofKey(nameField).one() }
            val textToInsert = "is_triggered_only = yes"
            block.addAfter(ParadoxScriptElementFactory.createProperty(project, textToInsert), insertAfterElement)
            block.addAfter(ParadoxScriptElementFactory.createLine(project), insertAfterElement)
        }
    }
}
