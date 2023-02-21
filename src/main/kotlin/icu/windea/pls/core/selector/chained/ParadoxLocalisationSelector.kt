package icu.windea.pls.core.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationSelector(project: Project, context: Any? = null) : ChainedParadoxSelector<ParadoxLocalisationProperty>(project, context)

fun localisationSelector(project: Project, context: Any? = null) = ParadoxLocalisationSelector(project, context)

fun ParadoxLocalisationSelector.locale(locale: CwtLocalisationLocaleConfig?) =
    apply { if(locale != null) selectors += ParadoxLocaleSelector(locale) }

@JvmOverloads
fun ParadoxLocalisationSelector.preferLocale(locale: CwtLocalisationLocaleConfig?, condition: Boolean = true) =
    apply { if(locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale) }

fun ParadoxLocalisationSelector.distinctByName() =
    distinctBy { it.name }