package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

object ParadoxDynamicValueService {
    fun resolveNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        val selector = ParadoxLocalisationSearch.selector(contextElement.project, contextElement).contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.searchNormal(name, selector).find()
    }

    fun resolveNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<ParadoxLocalisationProperty> {
        val selector = ParadoxLocalisationSearch.selector(contextElement.project, contextElement).contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
    }
}
