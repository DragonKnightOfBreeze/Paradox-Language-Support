package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.indexing.ID
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.index.CwtConfigSymbolIndexInfo
import icu.windea.pls.model.index.ParadoxDefineIndexInfo
import icu.windea.pls.model.index.ParadoxFilePathIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object PlsIndexKeys {
    val ScriptedVariableName = StubIndexKey.createIndexKey<String, ParadoxScriptScriptedVariable>("paradox.scriptedVariable.name.index")
    val DefinitionName = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index")
    val DefinitionType = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.type.index")
    val LocalisationName = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
    val SyncedLocalisationName = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")

    val DefinitionNameForResource = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.resource")
    val DefinitionNameForEconomicCategory = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.economicCategory")
    val DefinitionNameForGameConcept = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.gameConcept")
    val DefinitionNameForSprite = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.sprite")
    val DefinitionNameForTextColor = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.textColor")
    val DefinitionNameForTextIcon = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.textIcon")
    val DefinitionNameForTextFormat = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.textFormat")

    val LocalisationNameForModifier = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.modifier")
    val LocalisationNameForEvent = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.event")
    val LocalisationNameForTech = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.tech")

    // for inline script usages, inlineScriptExpression -> ParadoxScriptProperty
    val InlineScriptUsage = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.inlineScriptUsage.index")
    // for inline script arguments, inlineScriptExpression -> ParadoxScriptProperty
    val InlineScriptArgument = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.inlineScriptArgument.index")

    // #252
    // for definition injections, definitionName -> ParadoxScriptProperty
    val DefinitionInjectionTarget = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definitionInjection.name.index")

    val ConfigSymbol = ID.create<String, List<CwtConfigSymbolIndexInfo>>("cwt.config.symbol.index")
    val FileLocale = ID.create<String, Void>("paradox.file.locale.index")
    val FilePath = ID.create<String, ParadoxFilePathIndexInfo>("paradox.file.path.index")
    val Define = ID.create<String, Map<String, ParadoxDefineIndexInfo>>("paradox.define.index")
    val Merged = ID.create<String, List<ParadoxIndexInfo>>("paradox.merged.index")
}
