package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.indexing.ID
import icu.windea.pls.script.psi.ParadoxScriptProperty
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
    // TODO INLINE_DEFINITION: 移除基于文件的内联脚本使用索引（已迁移到 StubIndex）
    val InlineScriptUsage = ID.create<String, ParadoxInlineScriptUsageIndexInfo.Compact>("paradox.inlineScriptUsage.index")
    // New stub-based index (expression -> ParadoxScriptProperty for inline_script usage)
    val InlineScriptUsageByExpression = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.inlineScriptUsage.byExpression.index")
    // New stub-based index (argumentName -> ParadoxScriptProperty for inline_script usage arguments)
    val InlineScriptArgumentByName = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.inlineScriptArgument.byName.index")
    val Merged = ID.create<String, List<ParadoxIndexInfo>>("paradox.merged.index")
}
