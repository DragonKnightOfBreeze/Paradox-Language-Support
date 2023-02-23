package icu.windea.pls.core.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*

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
}
