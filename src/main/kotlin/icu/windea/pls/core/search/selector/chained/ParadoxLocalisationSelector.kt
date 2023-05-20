package icu.windea.pls.core.search.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.localisation.psi.*

typealias ParadoxLocalisationSelector = ChainedParadoxSelector<ParadoxLocalisationProperty>

fun localisationSelector(project: Project, context: Any? = null) = ParadoxLocalisationSelector(project, context)

fun ParadoxLocalisationSelector.locale(locale: CwtLocalisationLocaleConfig?) =
    apply { if(locale != null) selectors += ParadoxLocaleSelector(locale) }

@JvmOverloads
fun ParadoxLocalisationSelector.preferLocale(locale: CwtLocalisationLocaleConfig?, condition: Boolean = true) =
    apply { if(locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale) }

fun ParadoxLocalisationSelector.distinctByName() =
    distinctBy { it.name }

//fun ParadoxLocalisationSelector.withModifierConstraint() = 
//    useIndexKey(ParadoxLocalisationNameIndex.ModifierIndex.KEY)

private class WithConstraintSelector(val constraint: ParadoxLocalisationNameIndex.Constraint) : ParadoxSelector<ParadoxLocalisationProperty>

fun ParadoxLocalisationSelector.withConstraint(constraint: ParadoxLocalisationNameIndex.Constraint) =
    apply { selectors += WithConstraintSelector(constraint) }

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationSelector.withModifierConstraint() = withConstraint(ParadoxLocalisationNameIndex.Constraint.Modifier)

fun ParadoxLocalisationSelector.getConstraint(): ParadoxLocalisationNameIndex.Constraint =
    selectors.findIsInstance<WithConstraintSelector>()?.constraint ?: ParadoxLocalisationNameIndex.Constraint.Default