package icu.windea.pls.lang.inspections

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 基于特定条件，禁用适用于脚本文件的代码检查。
 */
class ParadoxScriptInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        var current = element
        if (ParadoxInspectionManager.isSuppressedForDefinition(element, toolId)) return true
        while (current !is PsiFile) {
            current = current.parent ?: return false
            ProgressManager.checkCanceled()
            if (current is ParadoxScriptProperty || (current is ParadoxScriptValue && current.isBlockMember())) {
                if (ParadoxInspectionManager.isSuppressedInComment(current, toolId)) return true
                if (ParadoxInspectionManager.isSuppressedForDefinition(current, toolId)) return true
            }
        }
        if (ParadoxInspectionManager.isSuppressedInComment(current, toolId)) return true
        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        if (element == null) return SuppressQuickFix.EMPTY_ARRAY
        return buildList {
            run {
                val file = element.containingFile ?: return@run
                val fileName = file.name
                add(SuppressForFileFix(SuppressionUtil.ALL, fileName))
                add(SuppressForFileFix(toolId, fileName))
            }
            run {
                val definition = element.findParentDefinition()
                if (definition !is ParadoxScriptProperty) return@run
                val definitionInfo = definition.definitionInfo ?: return@run
                add(SuppressForDefinitionFix(toolId, definitionInfo))
            }
            add(SuppressForExpressionFix(toolId))
        }.toTypedArray()
    }

    private class SuppressForFileFix(
        private val toolId: String,
        private val fileName: String
    ) : SuppressByCommentFix(toolId, ParadoxScriptFile::class.java) {
        override fun getText(): String {
            return when (toolId) {
                SuppressionUtil.ALL -> PlsBundle.message("suppress.for.file.all", fileName)
                else -> PlsBundle.message("suppress.for.file", fileName)
            }
        }

        override fun getCommentsFor(container: PsiElement): MutableList<out PsiElement> {
            return ParadoxInspectionManager.getCommentsForSuppression(container).toMutableList()
        }

        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            if (container !is PsiFile) return
            val text = PlsStringConstants.suppressInspectionsTagName + " " + myID
            val comment = SuppressionUtil.createComment(project, text, ParadoxScriptLanguage)
            container.addAfter(comment, null)
        }
    }

    private class SuppressForDefinitionFix(
        toolId: String,
        private val definitionInfo: ParadoxDefinitionInfo
    ) : SuppressByCommentFix(toolId, ParadoxScriptProperty::class.java) {
        //definition here should be a property, not a file

        override fun getText(): String {
            return PlsBundle.message("suppress.for.definition", definitionInfo.name)
        }

        override fun getContainer(context: PsiElement?): PsiElement {
            return definitionInfo.element
        }

        override fun getCommentsFor(container: PsiElement): List<PsiElement> {
            return ParadoxInspectionManager.getCommentsForSuppression(container).toList()
        }

        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            super.createSuppression(project, element, container)
        }
    }

    private class SuppressForExpressionFix(
        toolId: String
    ) : SuppressByCommentFix(toolId, ParadoxScriptMemberElement::class.java) {
        //here just call scriptMemberElement (property / value) "expression"

        override fun getText(): String {
            return PlsBundle.message("suppress.for.expression")
        }

        override fun getContainer(context: PsiElement?): PsiElement? {
            if (context == null) return null
            return context.parents(true).find { it is ParadoxScriptProperty || (it is ParadoxScriptValue && it.isBlockMember()) }
        }

        override fun getCommentsFor(container: PsiElement): List<PsiElement> {
            return ParadoxInspectionManager.getCommentsForSuppression(container).toList()
        }

        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            super.createSuppression(project, element, container)
        }
    }
}

