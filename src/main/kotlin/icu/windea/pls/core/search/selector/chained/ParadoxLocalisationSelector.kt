package icu.windea.pls.core.search.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.localisation.psi.*

typealias ParadoxLocalisationSelector = ChainedParadoxSelector<ParadoxLocalisationProperty>

fun localisationSelector(project: Project, context: Any? = null) = ParadoxLocalisationSelector(project, context)

fun ParadoxLocalisationSelector.locale(locale: CwtLocalisationLocaleConfig?) =
    apply { if(locale != null) selectors += ParadoxLocaleSelector(locale) }

fun ParadoxLocalisationSelector.preferLocale(locale: CwtLocalisationLocaleConfig?, condition: Boolean = true) =
    apply { if(locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale) }

fun ParadoxLocalisationSelector.distinctByName() =
    distinctBy { it.name }

class WithConstraintSelector(val constraint: ParadoxLocalisationConstraint) : ParadoxSelector<ParadoxLocalisationProperty>

fun ParadoxLocalisationSelector.withConstraint(constraint: ParadoxLocalisationConstraint) =
    apply { selectors += WithConstraintSelector(constraint) }

fun ParadoxLocalisationSelector.getConstraint(): ParadoxLocalisationConstraint =
    selectors.findIsInstance<WithConstraintSelector>()?.constraint ?: ParadoxLocalisationConstraint.Default