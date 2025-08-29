package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.AbstractFindUsagesDialog
import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiElement

//com.intellij.find.findUsages.JavaFindUsagesHandler

open class ParadoxFindUsagesHandler(
    private val element: PsiElement,
    private val factory: ParadoxFindUsagesHandlerFactory
) : FindUsagesHandler(element) {
    override fun getFindUsagesDialog(isSingleFile: Boolean, toShowInNewTab: Boolean, mustOpenInNewTab: Boolean): AbstractFindUsagesDialog {
        return ParadoxFindUsagesDialog(element, project, factory.findOptions, toShowInNewTab, mustOpenInNewTab, isSingleFile, this)
    }

    override fun getFindUsagesOptions(dataContext: DataContext?): ParadoxFindUsagesOptions {
        return factory.findOptions
    }

    override fun isSearchForTextOccurrencesAvailable(psiElement: PsiElement, isSingleFile: Boolean): Boolean {
        return true
    }
}
