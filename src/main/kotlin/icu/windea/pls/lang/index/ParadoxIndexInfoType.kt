package icu.windea.pls.lang.index

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*

abstract class ParadoxIndexInfoType<T : ParadoxIndexInfo>(val id: Byte) {
    data object ComplexEnumValue : ParadoxIndexInfoType<ParadoxComplexEnumValueIndexInfo>(1)
    data object DynamicValue : ParadoxIndexInfoType<ParadoxDynamicValueIndexInfo>(2)
    data object Parameter : ParadoxIndexInfoType<ParadoxParameterIndexInfo>(3)
    data object LocalisationParameter : ParadoxIndexInfoType<ParadoxLocalisationParameterIndexInfo>(4)

    data object InferredScopeContextAwareDefinition : ParadoxIndexInfoType<ParadoxInferredScopeContextAwareDefinitionIndexInfo>(5)
    data object EventInOnAction : ParadoxIndexInfoType<ParadoxEventInOnActionIndexInfo>(6)
    data object EventInEvent : ParadoxIndexInfoType<ParadoxEventInEventIndexInfo>(7)
    data object OnActionInEvent : ParadoxIndexInfoType<ParadoxOnActionInEventIndexInfo>(8)

    fun findInfos(file: VirtualFile, project: Project): List<T> {
        val index = findFileBasedIndex<ParadoxMergedIndex>()
        val fileData = index.getFileData(file, project)
        return getInfos(fileData)
    }

    fun processQuery(
        fileType: LanguageFileType,
        project: Project,
        gameType: ParadoxGameType,
        scope: GlobalSearchScope,
        processor: (file: VirtualFile, fileData: List<T>) -> Boolean
    ): Boolean {
        ProgressManager.checkCanceled()
        if (SearchScope.isEmptyScope(scope)) return true

        val index = findFileBasedIndex<ParadoxMergedIndex>()
        return FileTypeIndex.processFiles(fileType, p@{ file ->
            ProgressManager.checkCanceled()
            if (selectGameType(file) != gameType) return@p true //check game type at file level

            val fileData = index.getFileData(file, project)
            val infos = getInfos(fileData)
            if (infos.isEmpty()) return@p true
            processor(file, infos)
        }, scope)
    }

    fun getInfos(fileData: Map<String, List<ParadoxIndexInfo>>): List<T> {
        return fileData.get(id.toString())?.castOrNull<List<T>>().orEmpty()
    }
}
