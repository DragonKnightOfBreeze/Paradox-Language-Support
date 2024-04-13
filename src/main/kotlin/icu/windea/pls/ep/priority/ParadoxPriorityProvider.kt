package icu.windea.pls.ep.priority

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*

/**
 * 用于基于覆盖顺序对文件、封装变量、定义、本地化等进行排序。
 *
 * 默认使用后读覆盖排序。（[ParadoxPriority.LIOS]）
 *
 * @see ParadoxPriority
 */
@WithGameTypeEP
interface ParadoxPriorityProvider {
    fun getPriority(target: Any): ParadoxPriority?
    
    fun getPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxPriorityProvider>("icu.windea.pls.priorityProvider")
        
        fun getPriority(target: Any): ParadoxPriority {
            val gameType = selectGameType(target)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(gameType != null && !gameType.supportsByAnnotation(ep)) return@f null
                ep.getPriority(target)
            } ?: ParadoxPriority.LIOS
        }
        
        fun getPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority {
            val gameType = searchParameters.selector.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(gameType != null && !gameType.supportsByAnnotation(ep)) return@f null
                ep.getPriority(searchParameters)
            } ?: ParadoxPriority.LIOS
        }
        
        fun <T> getComparator(searchParameters: ParadoxSearchParameters<T>): Comparator<T> {
            val priority = getPriority(searchParameters)
            return Comparator c@{ o1, o2 ->
                val file1 = selectFile(o1) ?: return@c 1
                val file2 = selectFile(o2) ?: return@c -1
                if(file1 == file2) {
                    //同一文件中后面的总是会覆盖前面的
                    if(searchParameters is ParadoxFilePathSearch.SearchParameters) return@c 0
                    return@c 1
                }
                val fileInfo1 = file1.fileInfo ?: return@c 1
                val fileInfo2 = file2.fileInfo ?: return@c -1
                val path1 = fileInfo1.pathToEntry.path
                val path2 = fileInfo2.pathToEntry.path
                val pathResult = path1.compareTo(path2)
                if(pathResult != 0) {
                    //文件路径不同时，基于优先级进行排序
                    return@c when(priority) {
                        ParadoxPriority.FIOS -> pathResult
                        ParadoxPriority.LIOS -> -pathResult
                        ParadoxPriority.ORDERED -> pathResult
                    }
                }
                val rootFile = searchParameters.selector.rootFile ?: return@c 1
                val rootInfo = rootFile.fileInfo?.rootInfo ?: return@c 1
                val rootPath = rootFile.path
                val settings = when(rootInfo) {
                    is ParadoxGameRootInfo -> getProfilesSettings().gameSettings.get(rootPath)
                    is ParadoxModRootInfo -> getProfilesSettings().modSettings.get(rootPath)
                } ?: return@c 1
                val order1 = getOrder(fileInfo1, settings)
                val order2 = getOrder(fileInfo2, settings)
                val orderResult = order1.compareTo(order2)
                if(orderResult != 0) {
                    //文件路径相同时，总是会按照order由大到小排序
                    return@c -orderResult
                }
                1 //这里按照原有顺序进行排序
            }
        }
        
        private fun getOrder(fileInfo: ParadoxFileInfo, settings: ParadoxGameOrModSettingsState): Int {
            val rootPath = fileInfo.rootInfo.rootFile.path
            if(rootPath == settings.gameDirectory) return 0
            val i = settings.modDependencies.indexOfFirst { it.modDirectory == rootPath }
            if(i != -1) return i + 1
            if(settings is ParadoxModSettingsState && rootPath == settings.modDirectory) return Int.MAX_VALUE
            return -1
        }
    }
}