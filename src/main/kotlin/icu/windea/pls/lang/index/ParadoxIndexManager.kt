package icu.windea.pls.lang.index

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*

object ParadoxIndexManager {
    val ScriptedVariableNameKey = StubIndexKey.createIndexKey<String, ParadoxScriptScriptedVariable>("paradox.scriptedVariable.name.index")
    val DefinitionNameKey = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index")
    val DefinitionNameForTextFormatKey = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index.textFormat")
    val DefinitionTypeKey = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.type.index")
    val LocalisationNameKey = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
    val LocalisationNameForModifierKey = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.modifier")
    val SyncedLocalisationNameKey = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")

    val FilePath by lazy { findFileBasedIndex<ParadoxFilePathIndex>() }
    val FileLocale by lazy { findFileBasedIndex<ParadoxFileLocaleIndex>() }
    val Define by lazy { findFileBasedIndex<ParadoxDefineIndex>() }
    val InlineScriptUsage by lazy { findFileBasedIndex<ParadoxInlineScriptUsageIndex>() }
    val Merged by lazy { findFileBasedIndex<ParadoxMergedIndex>() }

    val FilePathName = ID.create<String, ParadoxFilePathIndex.Info>("paradox.file.path.index")
    val FileLocaleName = ID.create<String, Void>("paradox.file.locale.index")
    val DefineName = ID.create<String, Map<String, ParadoxDefineIndexInfo.Compact>>("paradox.define.index")
    val InlineScriptUsageName = ID.create<String, ParadoxInlineScriptUsageIndexInfo.Compact>("paradox.inlineScriptUsage.index")
    val MergedName = ID.create<String, List<ParadoxIndexInfo>>("paradox.merged.index")

    val excludeDirectoriesForFilePathIndex = listOf(
        "_CommonRedist",
        "crash_reporter",
        "curated_save_games",
        "pdx_browser",
        "pdx_launcher",
        "pdx_online_assets",
        "previewer_assets",
        "tweakergui_assets",
        "jomini",
    )

    val indexInfoMarkerKey = createKey<Boolean>("paradox.merged.info.index.marker")

    fun <ID : ParadoxIndexInfoType<T>, T : ParadoxIndexInfo> processQueryForMergedIndex(
        fileType: LanguageFileType,
        id: ID,
        project: Project,
        gameType: ParadoxGameType,
        scope: GlobalSearchScope,
        processor: (file: VirtualFile, fileData: List<T>) -> Boolean
    ): Boolean {
        ProgressManager.checkCanceled()
        if (SearchScope.isEmptyScope(scope)) return true

        return FileTypeIndex.processFiles(fileType, p@{ file ->
            ProgressManager.checkCanceled()
            if (selectGameType(file) != gameType) return@p true //check game type at file level

            val fileData = Merged.getFileData(file, project, id)
            if (fileData.isEmpty()) return@p true
            processor(file, fileData)
        }, scope)
    }
}
