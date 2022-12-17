package icu.windea.pls.core.selector.chained

import com.intellij.psi.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

class ParadoxComplexEnumValueSelector: ChainedParadoxSelector<ParadoxScriptStringExpressionElement>()

fun complexEnumValueSelector() = ParadoxComplexEnumValueSelector()

/**
 * 目前仅支持：[ParadoxComplexEnumValueSelector]
 * @see ParadoxSearchScope
 */
fun ParadoxComplexEnumValueSelector.withSearchScope(searchScope: String?, context: PsiElement) =
	apply { if(searchScope != null) selectors += ParadoxWithSearchScopeSelector(ParadoxSearchScope(searchScope), context) }

fun ParadoxComplexEnumValueSelector.distinctByName() =
	distinctBy { ParadoxComplexEnumValueHandler.getName(it) }