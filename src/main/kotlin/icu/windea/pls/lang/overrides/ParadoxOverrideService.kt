package icu.windea.pls.lang.overrides

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.ep.overrides.ParadoxOverrideStrategyProvider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.ParadoxSearchParameters
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxOverrideService {
    /**
     * 得到目标（文件、全局封装变量、定义、本地化等）使用的覆盖方式。
     * 如果返回 `null`，则表示不适用覆盖方式。
     */
    fun getOverrideStrategy(target: Any): ParadoxOverrideStrategy? {
        val gameType by lazy { selectGameType(target) }
        return ParadoxOverrideStrategyProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (gameType != null && !PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.get(target)
        }
    }

    /**
     * 从查询参数得到目标（文件、全局封装变量、定义、本地化等）使用的覆盖方式。
     * 如果返回 `null`，则表示不适用覆盖方式。
     */
    fun getOverrideStrategy(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy? {
        val gameType = searchParameters.selector.gameType
        return ParadoxOverrideStrategyProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (gameType != null && !PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.get(searchParameters)
        }
    }

    /**
     * 得到基于覆盖顺序的目标的排序器。
     */
    fun <T> getOverrideComparator(searchParameters: ParadoxSearchParameters<T>): ParadoxOverrideComparator<T> {
        return ParadoxOverrideComparator(searchParameters)
    }

    /**
     * 得到根目录路径 [rootPath] 在当前上下文中的顺序。
     */
    fun getOrderInContext(rootPath: String, settings: ParadoxGameOrModSettingsState): Int {
        if (rootPath == settings.gameDirectory) return 0
        val i = settings.modDependencies.indexOfFirst { it.modDirectory == rootPath }
        if (i != -1) return i + 1
        if (settings is ParadoxModSettingsState && rootPath == settings.modDirectory) return Int.MAX_VALUE
        return -1
    }

    /**
     * 检查是否存在对文件的重载。
     * 如果返回 `null`，则表示使用的覆盖方式为 `ORDERED`，或者不存在重载。
     */
    fun getOverrideResultForFile(file: PsiFile): ParadoxOverrideResult<PsiFile>? {
        val overrideStrategy = getOverrideStrategy(file) ?: return null
        if (overrideStrategy == ParadoxOverrideStrategy.ORDERED) return null
        val fileInfo = file.fileInfo ?: return null
        if (!ParadoxFileManager.canOverrideFile(file, fileInfo.fileType)) return null
        val path = fileInfo.path.path
        val project = file.project
        val selector = selector(project, file).file()
        val results = ParadoxFilePathSearch.search(path, null, selector).findAll().mapNotNull { it.toPsiFile(project) }
        if (results.size < 2) return null // no override -> skip
        return ParadoxOverrideResult(path, file, results, overrideStrategy)
    }

    /**
     * 检查是否存在对（全局）封装变量的重载。
     * 如果返回 `null`，则表示使用的覆盖方式为 `ORDERED`，或者不存在重载。
     */
    fun getOverrideResultForGlobalScriptedVariable(element: ParadoxScriptScriptedVariable, file: PsiFile): ParadoxOverrideResult<ParadoxScriptScriptedVariable>? {
        val name = element.name
        if (name.isNullOrEmpty()) return null // anonymous -> skipped
        if (name.isParameterized()) return null // parameterized -> ignored
        val overrideStrategy = getOverrideStrategy(element) ?: return null
        if (overrideStrategy == ParadoxOverrideStrategy.ORDERED) return null
        val project = file.project
        val selector = selector(project, file).scriptedVariable()
        val results = ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll().toList()
        if (results.size < 2) return null // no override -> skip
        return ParadoxOverrideResult(name, element, results, overrideStrategy)
    }

    /**
     * 检查是否存在对定义的重载。
     * 如果返回 `null`，则表示使用的覆盖方式为 `ORDERED`，或者不存在重载。
     */
    fun getOverrideResultForDefinition(element: ParadoxScriptProperty, file: PsiFile): ParadoxOverrideResult<ParadoxScriptProperty>? {
        val definitionInfo = element.definitionInfo ?: return null
        val name = definitionInfo.name
        val type = definitionInfo.type
        if (name.isEmpty()) return null // anonymous -> skipped
        if (name.isParameterized()) return null // parameterized -> ignored
        val overrideStrategy = getOverrideStrategy(element) ?: return null
        if (overrideStrategy == ParadoxOverrideStrategy.ORDERED) return null
        val project = file.project
        val selector = selector(project, file).definition()
        val results = ParadoxDefinitionSearch.search(name, type, selector).findAll().filterIsInstance<ParadoxScriptProperty>()
        if (results.size < 2) return null // no override -> skip
        return ParadoxOverrideResult(name, element, results, overrideStrategy)
    }

    /**
     * 检查对目标的重载是否正确。
     * - `FIOS` `LIOS` - 目标必须与第一个重载项拥有相同的文件路径和根目录。
     * - `DUPL` - 目标必须与第一个来自游戏文件的重载项拥有相同的文件路径。
     * - `ORDERED` - 总是正确。
     */
    fun <T : PsiElement> isOverrideCorrect(overrideResult: ParadoxOverrideResult<T>): Boolean {
        val target = overrideResult.target
        val results = overrideResult.results
        val overrideStrategy = overrideResult.overrideStrategy

        if (target is PsiFileSystemItem) return true
        when (overrideStrategy) {
            ParadoxOverrideStrategy.FIOS, ParadoxOverrideStrategy.LIOS -> {
                // require same file path VS first result (injected roots are ignored)
                val fileInfo = selectFile(target)?.fileInfo?.takeIf { it.rootInfo is ParadoxRootInfo.MetadataBased }
                if (fileInfo == null) return true
                val firstFileInfo = results.firstNotNullOfOrNull { r ->
                    selectFile(r)?.fileInfo?.takeIf { it.rootInfo is ParadoxRootInfo.MetadataBased }
                }
                if (firstFileInfo == null) return true
                return isSameFilePath(fileInfo, firstFileInfo) && isSameRootDirectory(fileInfo, firstFileInfo)
            }
            ParadoxOverrideStrategy.DUPL -> {
                // require same file path VS vanilla result (injected roots are ignored)
                val fileInfo = selectFile(overrideResult.target)?.fileInfo
                if (fileInfo == null) return true
                val vanillaFileInfo = results.firstNotNullOfOrNull { r ->
                    selectFile(r)?.fileInfo?.takeIf { it.rootInfo is ParadoxRootInfo.Game }
                }
                if (vanillaFileInfo == null) return true
                return isSameFilePath(fileInfo, vanillaFileInfo)
            }
            ParadoxOverrideStrategy.ORDERED -> {
                // always true
                return true
            }
        }
    }

    private fun isSameRootDirectory(fileInfo1: ParadoxFileInfo, fileInfo2: ParadoxFileInfo): Boolean {
        val rootInfo1 = fileInfo1.rootInfo.castOrNull<ParadoxRootInfo.MetadataBased>() ?: return true
        val rootInfo2 = fileInfo2.rootInfo.castOrNull<ParadoxRootInfo.MetadataBased>() ?: return true
        return rootInfo1.rootFile == rootInfo2.rootFile
    }

    private fun isSameFilePath(fileInfo1: ParadoxFileInfo, fileInfo2: ParadoxFileInfo): Boolean {
        return fileInfo1.path == fileInfo2.path
    }
}
