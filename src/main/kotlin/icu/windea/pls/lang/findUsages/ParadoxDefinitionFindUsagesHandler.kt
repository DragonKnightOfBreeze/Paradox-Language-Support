package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.AbstractFindUsagesDialog
import com.intellij.openapi.actionSystem.DataContext
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class ParadoxDefinitionFindUsagesHandler(
    private val element: ParadoxScriptDefinitionElement,
    private val factory: ParadoxFindUsagesHandlerFactory
) : ParadoxFindUsagesHandler(element, factory) {
    override fun getFindUsagesDialog(isSingleFile: Boolean, toShowInNewTab: Boolean, mustOpenInNewTab: Boolean): AbstractFindUsagesDialog {
        return ParadoxFindDefinitionUsagesDialog(element, project, factory.findDefinitionOptions, toShowInNewTab, mustOpenInNewTab, isSingleFile, this)
    }

    override fun getFindUsagesOptions(dataContext: DataContext?): ParadoxDefinitionFindUsagesOptions {
        return factory.findDefinitionOptions
    }
}
