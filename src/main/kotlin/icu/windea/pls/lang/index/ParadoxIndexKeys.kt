package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.indexing.ID
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.indexInfo.ParadoxDefineIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxInlineScriptUsageIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxIndexKeys {
    val ScriptedVariableName = StubIndexKey.createIndexKey<String, ParadoxScriptScriptedVariable>("paradox.scriptedVariable.name.index")
    val DefinitionName = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index")
    val DefinitionType = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.type.index")
    val LocalisationName = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
    val SyncedLocalisationName = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")

    val DefinitionNameForTextFormat = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.textFormat")

    val LocalisationNameForModifier = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.modifier")
    val LocalisationNameForEvent = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.event")
    val LocalisationNameForTech = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.tech")

    val FilePath = ID.create<String, ParadoxFilePathIndex.Info>("paradox.file.path.index")
    val FileLocale = ID.create<String, Void>("paradox.file.locale.index")
    val Define = ID.create<String, Map<String, ParadoxDefineIndexInfo>>("paradox.define.index")
    val InlineScriptUsage = ID.create<String, ParadoxInlineScriptUsageIndexInfo.Compact>("paradox.inlineScriptUsage.index")
    val Merged = ID.create<String, List<ParadoxIndexInfo>>("paradox.merged.index")
}
