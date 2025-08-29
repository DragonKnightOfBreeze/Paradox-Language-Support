package icu.windea.pls.lang.refactoring.inline

import com.intellij.history.LocalHistory
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.listeners.RefactoringEventData
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.processQuery
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.listOrEmpty
import icu.windea.pls.core.util.or
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxPsiManager
import icu.windea.pls.script.psi.ParadoxScriptFile

class ParadoxInlineScriptInlineProcessor(
    project: Project,
    scope: GlobalSearchScope,
    private var element: ParadoxScriptFile,
    private val reference: PsiReference?,
    private val editor: Editor?,
    private val inlineThisOnly: Boolean,
    private val keepTheDeclaration: Boolean,
) : BaseRefactoringProcessor(project, scope, null) {
    //do not use DescriptiveNameUtil.getDescriptiveName(element) here
    private val descriptiveName = ParadoxInlineScriptManager.getInlineScriptExpression(element).or.anonymous()

    override fun getCommandName() = PlsBundle.message("inline.inlineScript.command", descriptiveName)

    override fun createUsageViewDescriptor(usages: Array<out UsageInfo>) = ParadoxInlineViewDescriptor(element)

    override fun findUsages(): Array<UsageInfo> {
        if (inlineThisOnly) {
            if (reference == null) return UsageInfo.EMPTY_ARRAY
            return arrayOf(UsageInfo(reference))
        }
        val usages = mutableSetOf<UsageInfo>()
        if (reference != null) {
            usages.add(UsageInfo(reference.element))
        }
        ReferencesSearch.search(element, myRefactoringScope, true).processQuery p@{ reference ->
            ProgressManager.checkCanceled()
            if (ParadoxInlineScriptManager.getContextReferenceElement(reference.element) == null) return@p true
            usages.add(UsageInfo(reference.element))
        }
        return usages.toTypedArray()
    }

    override fun refreshElements(elements: Array<out PsiElement>) {
        val newElement = elements.singleOrNull()?.castOrNull<ParadoxScriptFile>() ?: return
        element = newElement
    }

    override fun preprocessUsages(refUsages: Ref<Array<UsageInfo>>): Boolean {
        return super.preprocessUsages(refUsages)
    }

    override fun getRefactoringId(): String {
        return "pls.refactoring.inline.inlineScript"
    }

    override fun getBeforeData(): RefactoringEventData {
        return RefactoringEventData().apply { addElement(element) }
    }

    override fun getElementsToWrite(descriptor: UsageViewDescriptor): Collection<PsiElement> {
        return if (inlineThisOnly) {
            reference?.element.singleton.listOrEmpty()
        } else {
            if (!element.isWritable) return emptyList()
            if (reference == null) element.singleton.list() else listOf(reference.element, element)
        }
    }

    override fun performRefactoring(usages: Array<out UsageInfo>) {
        var position: RangeMarker? = null
        if (editor != null) {
            val offset: Int = editor.caretModel.offset
            position = editor.document.createRangeMarker(offset, offset + 1)
        }
        val a = LocalHistory.getInstance().startAction(getCommandName())
        try {
            doRefactoring(usages)
        } finally {
            a.finish()
        }
        if (position != null) {
            if (editor != null && position.isValid) {
                editor.caretModel.moveToOffset(position.startOffset)
            }
            position.dispose()
        }
    }

    private fun doRefactoring(usages: Array<out UsageInfo>) {
        for (usage in usages) {
            val usageElement = usage.element ?: continue
            val rangeInUsageElement = usage.rangeInElement ?: continue
            try {
                ParadoxPsiManager.inlineInlineScript(usageElement, rangeInUsageElement, element, myProject)
            } catch (e: IncorrectOperationException) {
                thisLogger().error(e)
            }
        }

        if (!inlineThisOnly && !keepTheDeclaration) {
            //删除对应的内联脚本文件
            try {
                element.delete()
            } catch (e: IncorrectOperationException) {
                thisLogger().error(e)
            }
        }
    }
}
