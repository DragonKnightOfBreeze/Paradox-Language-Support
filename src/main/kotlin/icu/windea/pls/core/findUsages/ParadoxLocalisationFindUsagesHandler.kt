package icu.windea.pls.core.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.actionSystem.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationFindUsagesHandler(
    private val element: ParadoxLocalisationProperty,
    private val factory: ParadoxFindUsagesHandlerFactory
) : ParadoxFindUsagesHandler(element, factory) {
    val locale by lazy { element.localeConfig }
    
    override fun getFindUsagesDialog(isSingleFile: Boolean, toShowInNewTab: Boolean, mustOpenInNewTab: Boolean): AbstractFindUsagesDialog {
        return ParadoxFindLocalisationUsagesDialog(element, project, factory.findLocalisationOptions, toShowInNewTab, mustOpenInNewTab, isSingleFile, this)
    }
    
    override fun getFindUsagesOptions(dataContext: DataContext?): ParadoxLocalisationFindUsagesOptions {
        return factory.findLocalisationOptions
    }
}