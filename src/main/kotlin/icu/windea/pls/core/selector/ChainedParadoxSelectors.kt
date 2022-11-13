package icu.windea.pls.core.selector

import com.intellij.openapi.vfs.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class NopParadoxSelector<T>: ChainedParadoxSelector<T>()

class ParadoxFileSelector: ChainedParadoxSelector<VirtualFile>()

class ParadoxScriptedVariableSelector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>()

class ParadoxDefinitionSelector: ChainedParadoxSelector<ParadoxDefinitionProperty>()

class ParadoxLocalisationSelector: ChainedParadoxSelector<ParadoxLocalisationProperty>()

class ParadoxComplexEnumValueSelector: ChainedParadoxSelector<ParadoxScriptExpressionElement>()

class ParadoxValueSetValueSelector: ChainedParadoxSelector<ParadoxScriptString>()


fun <T> nopSelector() = NopParadoxSelector<T>()

fun fileSelector() = ParadoxFileSelector()

fun scriptedVariableSelector() = ParadoxScriptedVariableSelector()

fun definitionSelector() = ParadoxDefinitionSelector()

fun localisationSelector() = ParadoxLocalisationSelector()

fun complexEnumValueSelector() = ParadoxComplexEnumValueSelector()

fun valueSetValueSelector() = ParadoxValueSetValueSelector()