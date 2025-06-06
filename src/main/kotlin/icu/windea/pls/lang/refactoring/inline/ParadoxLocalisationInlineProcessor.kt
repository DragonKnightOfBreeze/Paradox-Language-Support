package icu.windea.pls.lang.refactoring.inline

import com.intellij.history.*
import com.intellij.lang.findUsages.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.refactoring.*
import com.intellij.refactoring.listeners.*
import com.intellij.usageView.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationInlineProcessor(
    project: Project,
    scope: GlobalSearchScope,
    private var element: ParadoxLocalisationProperty,
    private val reference: PsiReference?,
    private val editor: Editor?,
    private val inlineThisOnly: Boolean,
    private val keepTheDeclaration: Boolean,
) : BaseRefactoringProcessor(project, scope, null) {
    private val descriptiveName = DescriptiveNameUtil.getDescriptiveName(element)

    override fun getCommandName() = PlsBundle.message("inline.localisation.command", descriptiveName)

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
            usages.add(UsageInfo(reference.element))
        }
        return usages.toTypedArray()
    }

    override fun refreshElements(elements: Array<out PsiElement>) {
        val newElement = elements.singleOrNull()?.castOrNull<ParadoxLocalisationProperty>() ?: return
        element = newElement
    }

    override fun preprocessUsages(refUsages: Ref<Array<UsageInfo>>): Boolean {
        return super.preprocessUsages(refUsages)
    }

    override fun getRefactoringId(): String {
        return "pls.refactoring.inline.localisation"
    }

    override fun getBeforeData(): RefactoringEventData {
        return RefactoringEventData().apply { addElement(element) }
    }

    override fun getElementsToWrite(descriptor: UsageViewDescriptor): Collection<PsiElement> {
        return if (inlineThisOnly) {
            reference?.element.toSingletonListOrEmpty()
        } else {
            if (!element.isWritable) return emptyList()
            if (reference == null) element.toSingletonList() else listOf(reference.element, element)
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
                ParadoxPsiManager.inlineLocalisation(usageElement, rangeInUsageElement, element, myProject)
            } catch (e: IncorrectOperationException) {
                thisLogger().error(e)
            }
        }

        if (!inlineThisOnly && !keepTheDeclaration) {
            try {
                element.delete()
            } catch (e: IncorrectOperationException) {
                thisLogger().error(e)
            }
        }
    }
}
