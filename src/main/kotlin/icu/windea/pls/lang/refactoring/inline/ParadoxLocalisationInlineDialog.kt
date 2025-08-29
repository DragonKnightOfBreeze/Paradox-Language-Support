package icu.windea.pls.lang.refactoring.inline

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.inline.InlineOptionsDialog
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class ParadoxLocalisationInlineDialog(
    project: Project,
    private val element: ParadoxLocalisationProperty,
    private val reference: PsiReference?,
    private val editor: Editor?
) : InlineOptionsDialog(project, true, element) {
    private val optimizedScope = ParadoxSearchScope.fromElement(element)
        ?.withFileTypes(ParadoxLocalisationFileType)
        ?.intersectWith(GlobalSearchScope.projectScope(project))
        ?: GlobalSearchScope.projectScope(project)

    init {
        title = PlsBundle.message("title.inline.localisation")
        myInvokedOnReference = reference != null
        init()
        helpAction.isEnabled = false
    }

    override fun getNameLabelText(): String {
        val name = element.name.or.anonymous()
        return PlsBundle.message("inline.localisation.label", name)
    }

    override fun getInlineThisText(): String {
        return PlsBundle.message("inline.localisation.inline.this")
    }

    override fun getInlineAllText(): String {
        return if (element.isWritable) PlsBundle.message("inline.localisation.inline.all.remove")
        else PlsBundle.message("inline.localisation.inline.all")
    }

    override fun getKeepTheDeclarationText(): String {
        return if (element.isWritable) PlsBundle.message("inline.localisation.inline.all.keep")
        else super.getKeepTheDeclarationText()
    }

    override fun allowInlineAll(): Boolean {
        return true
    }

    override fun isInlineThis(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineLocalisationThis
    }

    override fun isKeepTheDeclarationByDefault(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineLocalisationKeep
    }

    override fun doAction() {
        val processor = ParadoxLocalisationInlineProcessor(project, optimizedScope, element, reference, editor, isInlineThisOnly, isKeepTheDeclaration())
        invokeRefactoring(processor)
        val settings = ParadoxRefactoringSettings.getInstance()
        if (myRbInlineThisOnly.isEnabled && myRbInlineAll.isEnabled) {
            settings.inlineLocalisationThis = isInlineThisOnly
        }
        if (myKeepTheDeclaration != null && myKeepTheDeclaration!!.isEnabled) {
            settings.inlineLocalisationKeep = isKeepTheDeclaration()
        }
    }
}
