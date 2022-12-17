package icu.windea.pls.core.selector.chained

import icu.windea.pls.config.cwt.config.ext.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationSelector: ChainedParadoxSelector<ParadoxLocalisationProperty>()

fun localisationSelector() = ParadoxLocalisationSelector()

fun ParadoxLocalisationSelector.locale(locale: CwtLocalisationLocaleConfig?) =
	apply { if(locale != null) selectors += ParadoxLocaleSelector(locale) }

@JvmOverloads
fun ParadoxLocalisationSelector.preferLocale(locale: CwtLocalisationLocaleConfig?, condition: Boolean = true) =
	apply { if(locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale) }

fun ParadoxLocalisationSelector.distinctByName() =
	distinctBy { it.name }