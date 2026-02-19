package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.indexing.ID
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.index.CwtConfigSymbolIndexInfo
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.model.index.ParadoxDefineVariableKey
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
import icu.windea.pls.model.index.ParadoxFilePathData
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object PlsIndexKeys {
    val ScriptedVariableName = StubIndexKey.createIndexKey<String, ParadoxScriptScriptedVariable>("paradox.scriptedVariable.name.index")
    val LocalisationName = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
    val LocalisationNameForModifier = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.modifier")
    val LocalisationNameForEvent = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.event")
    val LocalisationNameForTech = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.tech")
    val SyncedLocalisationName = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")

    // for define namespaces, namespace -> ParadoxScriptProperty
    val DefineNamespace = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.define.namespace.index")
    // for define variables, (namespace, variable) -> ParadoxScriptProperty
    val DefineVariable = StubIndexKey.createIndexKey<ParadoxDefineVariableKey, ParadoxScriptProperty>("paradox.define.variable.index")

    // for inline script usages, expression -> ParadoxScriptProperty
    val InlineScriptUsage = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.inlineScriptUsage.index")
    // for inline script arguments, expression -> `ParadoxScriptProperty`
    val InlineScriptArgument = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.inlineScriptArgument.index")

    val ConfigSymbol = ID.create<String, List<CwtConfigSymbolIndexInfo>>("cwt.config.symbol.index")
    val FileLocale = ID.create<String, Void>("paradox.file.locale.index")
    val FilePath = ID.create<String, ParadoxFilePathData>("paradox.file.path.index")
    val ComplexEnumValue = ID.create<String, List<ParadoxComplexEnumValueIndexInfo>>("paradox.complexEnumValue.index")
    val Definition = ID.create<String, List<ParadoxDefinitionIndexInfo>>("paradox.definition.index")
    val DefinitionInjection = ID.create<String, List<ParadoxDefinitionInjectionIndexInfo>>("paradox.definitionInjection.index") // #252
    val Merged = ID.create<String, List<ParadoxIndexInfo>>("paradox.merged.index")
}
