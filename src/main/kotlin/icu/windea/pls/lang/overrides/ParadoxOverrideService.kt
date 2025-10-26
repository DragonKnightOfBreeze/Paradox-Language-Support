package icu.windea.pls.lang.overrides

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.ep.overrides.ParadoxOverrideStrategyProvider
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
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxOverrideService {
    /**
     * 得到目标（文件、全局封装变量、定义、本地化等）使用的覆盖方式。
     * 如果返回 `null`，则表示不适用覆盖方式。
     */
    fun getOverrideStrategy(target: Any): ParadoxOverrideStrategy? {
        return ParadoxOverrideStrategyProvider.get(target)
    }

    /**
     * 从查询参数得到目标（文件、全局封装变量、定义、本地化等）使用的覆盖方式。
     * 如果返回 `null`，则表示不适用覆盖方式。
     */
    fun getOverrideStrategy(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy? {
        return ParadoxOverrideStrategyProvider.get(searchParameters)
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

    fun <T : PsiElement> isOverrideCorrect(overrideResult: ParadoxOverrideResult<T>): Boolean {
        val target = overrideResult.target
        if (target is PsiFileSystemItem) return true
        val fileInfo = target.fileInfo ?: return true
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return true
        val firstResult = overrideResult.results.first()
        if (firstResult is PsiFileSystemItem) return true
        val firstFileInfo = firstResult.fileInfo ?: return true
        val firstRootInfo = firstFileInfo.rootInfo
        if (firstRootInfo !is ParadoxRootInfo.MetadataBased) return true
        // different root file -> incorrect override
        return firstRootInfo.rootFile == rootInfo.rootFile
    }
}
