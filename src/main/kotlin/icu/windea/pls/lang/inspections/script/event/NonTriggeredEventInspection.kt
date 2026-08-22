package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.psi.PsiFileOnlyVisitor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

class NonTriggeredEventInspection : EventInspectionBase() {
    // https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/88

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiFileOnlyVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                check(file, holder)
            }
        }
    }

    private fun check(file: PsiFile, holder: ProblemsHolder) {
        if (file !is ParadoxScriptFile) return
        val elements = file.properties(inline = true)
        for (element in elements) {
            ProgressManager.checkCanceled()
            val definitionInfo = element.definitionInfo ?: continue
            if (definitionInfo.type != ParadoxDefinitionTypes.event) continue
            if ("triggered" !in definitionInfo.typeConfig.subtypes.keys) continue  // no `triggered` subtype declared, skip
            if ("inherited" in definitionInfo.subtypes) continue  // ignore inherited events
            if ("triggered" in definitionInfo.subtypes) continue
            val description = ChronicleInspectionBundle.message("inspection.script.nonTriggeredEvent.desc")
            val fixes = getFixes(element)
            holder.registerProblem(element.propertyKey, description, *fixes)
        }
    }

    private fun getFixes(element: ParadoxScriptProperty): Array<LocalQuickFix> {
        val result = mutableListOf<LocalQuickFix>()
        if (element.block != null) result += Fix()
        return result.toArray(LocalQuickFix.EMPTY_ARRAY)
    }

    private class Fix : PsiUpdateModCommandQuickFix() {
        override fun getFamilyName() = ChronicleInspectionBundle.message("inspection.script.nonTriggeredEvent.fix.1.name")

        override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
            val element = element.parentOfType<ParadoxScriptProperty>(withSelf = true) ?: return
            val definitionInfo = element.definitionInfo ?: return
            val block = element.block ?: return
            val nameField = definitionInfo.typeConfig.nameField

            val textToInsert = "is_triggered_only = yes"
            val propertiesToDelete = selectScope { block.properties().ofKey("is_triggered_only").all() }
            val insertAfterElement = when {
                propertiesToDelete.isNotEmpty() -> propertiesToDelete.first()
                nameField == null -> null
                else -> selectScope { block.properties().ofKey(nameField).one() }
            }

            // add `is_triggered_only = yes` into declaration (after first existing `is_triggered_only` property, first `id` property, or at start)
            block.addAfter(ParadoxScriptElementFactory.createPropertyFromText(project, textToInsert), insertAfterElement)
            block.addAfter(ParadoxScriptElementFactory.createWhiteSpaceFromText(project, "\n"), insertAfterElement)

            // delete all `is_triggered_only` properties if exist
            propertiesToDelete.forEach { it.delete() }
        }
    }
}
