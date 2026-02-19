package icu.windea.pls.lang.inspections.suppress

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.parents
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.createPointer
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

/**
 * 基于特定条件，禁用适用于脚本文件的代码检查。
 */
class ParadoxScriptInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        var current = element
        if (PlsInspectionSuppressManager.isSuppressedForDefinition(element, toolId)) return true
        while (current !is PsiFile) {
            current = current.parent ?: return false
            ProgressManager.checkCanceled()
            if (current is ParadoxScriptProperty || (current is ParadoxScriptValue && current.isBlockMember())) {
                if (PlsInspectionSuppressManager.isSuppressedInComment(current, toolId)) return true
                if (PlsInspectionSuppressManager.isSuppressedForDefinition(current, toolId)) return true
            }
        }
        if (PlsInspectionSuppressManager.isSuppressedInComment(current, toolId)) return true
        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        if (element == null) return SuppressQuickFix.EMPTY_ARRAY
        val file = element.containingFile ?: return SuppressQuickFix.EMPTY_ARRAY
        return buildList {
            run {
                val fileName = file.name
                add(SuppressForFileFix(SuppressionUtil.ALL, fileName))
                add(SuppressForFileFix(toolId, fileName))
            }
            run {
                val definition = selectScope { element.parentDefinition().asProperty() } ?: return@run
                val definitionInfo = definition.definitionInfo ?: return@run
                val name = definitionInfo.name
                val containerPointer = definition.createPointer<PsiElement>(file)
                add(SuppressForDefinitionFix(toolId, name, containerPointer))
            }
            run {
                // 2.1.0 兼容定义注入
                val definitionInjection = selectScope { element.parentDefinitionInjection() } ?: return@run
                val definitionInjectionInfo = definitionInjection.definitionInjectionInfo ?: return@run
                val expression = definitionInjectionInfo.expression
                val containerPointer = definitionInjection.createPointer<PsiElement>(file)
                add(SuppressForDefinitionInjectionFix(toolId, expression, containerPointer))
            }
            add(SuppressForExpressionFix(toolId))
        }.toTypedArray()
    }

    private class SuppressForFileFix(
        private val toolId: String,
        private val fileName: String
    ) : ParadoxSuppressByCommentFix(toolId, ParadoxScriptFile::class.java) {
        override fun getText(): String {
            return when (toolId) {
                SuppressionUtil.ALL -> PlsBundle.message("suppress.for.file.all", fileName)
                else -> PlsBundle.message("suppress.for.file", fileName)
            }
        }

        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            if (container !is PsiFile) return
            val text = PlsStrings.suppressInspectionsTagName + " " + myID
            val comment = SuppressionUtil.createComment(project, text, ParadoxScriptLanguage)
            container.addAfter(comment, null)
        }
    }

    private class SuppressForDefinitionFix(
        toolId: String,
        private val name: String,
        private val containerPointer: SmartPsiElementPointer<PsiElement>
    ) : ParadoxSuppressByCommentFix(toolId, ParadoxScriptProperty::class.java) {
        // definition here should be a property, not a file

        override fun getText(): String {
            return PlsBundle.message("suppress.for.definition", name)
        }

        override fun getContainer(context: PsiElement?): PsiElement? {
            return containerPointer.element
        }
    }

    private class SuppressForDefinitionInjectionFix(
        toolId: String,
        private val name: String,
        private val containerPointer: SmartPsiElementPointer<PsiElement>
    ) : ParadoxSuppressByCommentFix(toolId, ParadoxScriptProperty::class.java) {
        // definition here should be a property, not a file

        override fun getText(): String {
            return PlsBundle.message("suppress.for.definitionInjection", name)
        }

        override fun getContainer(context: PsiElement?): PsiElement? {
            return containerPointer.element
        }
    }

    private class SuppressForExpressionFix(
        toolId: String
    ) : ParadoxSuppressByCommentFix(toolId, ParadoxScriptMember::class.java) {
        // here just call scriptMemberElement (property / value) "expression"

        override fun getText(): String {
            return PlsBundle.message("suppress.for.expression")
        }

        override fun getContainer(context: PsiElement?): PsiElement? {
            if (context == null) return null
            return context.parents(true).find { it is ParadoxScriptProperty || (it is ParadoxScriptValue && it.isBlockMember()) }
        }
    }
}

