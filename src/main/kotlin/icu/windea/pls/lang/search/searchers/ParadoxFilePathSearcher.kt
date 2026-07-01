package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.ParadoxGameType

/**
 * 文件路径的查询器。
 */
class ParadoxFilePathSearcher : QueryExecutorBase<VirtualFile, ParadoxFilePathSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxFilePathSearch.Parameters, consumer: Processor<in VirtualFile>) {
        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in VirtualFile>): Boolean {
        if (!context.isValid()) return true
        if (context.configExpression == null) {
            if (context.filePath == null) {
                val keys = FileBasedIndex.getInstance().getAllKeys(PlsIndexKeys.FilePath, context.project)
                return FileBasedIndex.getInstance().processFilesContainingAnyKey(PlsIndexKeys.FilePath, keys, context.scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
                    if (!matchesGameType(context, file)) return@p true // check game type at file level
                    consumer.process(file)
                }
            } else {
                if (context.filePath.isEmpty()) return true
                val keys = getFilePaths(context, context.filePath)
                return FileBasedIndex.getInstance().processFilesContainingAnyKey(PlsIndexKeys.FilePath, keys, context.scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
                    if (!matchesGameType(context, file)) return@p true // check game type at file level
                    consumer.process(file)
                }
            }
        } else {
            val support = ParadoxPathReferenceExpressionSupport.get(context.configExpression) ?: return true
            if (context.filePath == null) {
                val keys = mutableSetOf<String>()
                FileBasedIndex.getInstance().processAllKeys(PlsIndexKeys.FilePath, p@{ p ->
                    if (!support.matches(context.configExpression, context.contextElement, p)) return@p true
                    keys.add(p)
                }, context.scope, null)
                return FileBasedIndex.getInstance().processFilesContainingAnyKey(PlsIndexKeys.FilePath, keys, context.scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
                    if (!matchesGameType(context, file)) return@p true // check game type at file level
                    consumer.process(file)
                }
            } else {
                if (context.filePath.isEmpty()) return true
                val canResolve = support.canResolve(context.configExpression, context.filePath)
                if (!canResolve) return true
                val resolvedPaths = support.resolvePath(context.configExpression, context.filePath)
                if (resolvedPaths != null) return processResolvedPaths(context, resolvedPaths, consumer)
                val resolvedFileNames = support.resolveFileName(context.configExpression, context.filePath)
                if (resolvedFileNames != null) return processResolvedFileNames(context, resolvedFileNames, support, consumer)
                return true
            }
        }
    }

    private fun processResolvedPaths(context: Context, resolved: Set<String>, consumer: Processor<in VirtualFile>): Boolean {
        if (resolved.isEmpty()) return true
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(PlsIndexKeys.FilePath, resolved, context.scope, null, null) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
            if (!matchesGameType(context, file)) return@p true // check game type at file level
            consumer.process(file)
        }
    }

    private fun processResolvedFileNames(context: Context, resolved: Set<String>, support: ParadoxPathReferenceExpressionSupport, consumer: Processor<in VirtualFile>): Boolean {
        if (resolved.isEmpty()) return true
        val configExpression = context.configExpression ?: return true
        val resolvedFiles = sortedSetOf<VirtualFile>(compareBy { it.path })
        FilenameIndex.processFilesByNames(resolved, false, context.scope, null) p@{ file ->
            ProgressManager.checkCanceled()
            val fileInfo = ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
            if (!matchesGameType(context, file)) return@p true // check game type at file level
            if (!support.matches(configExpression, context.contextElement, fileInfo.path.path)) return@p true
            resolvedFiles.add(file)
        }
        return resolvedFiles.process { consumer.process(it) }
    }

    private fun getFilePaths(context: Context, filePath: String): Set<String> {
        if (context.ignoreLocale) {
            return getFilePathsIgnoreLocale(filePath) ?: setOf(filePath)
        } else {
            return setOf(filePath)
        }
    }

    private fun getFilePathsIgnoreLocale(filePath: String): Set<String>? {
        if (!filePath.endsWith(".yml", true)) return null // 仅限本地化文件
        val configGroup = ChronicleFacade.getConfigGroup()
        val globalLocales = ParadoxLocaleManager.getGlobalLocales(configGroup)
        val localeStrings = globalLocales.map { it.shortId }
        var index = 0
        var usedLocaleString: String? = null
        for (localeString in localeStrings) {
            val nextIndex = filePath.indexOf(localeString, index)
            if (nextIndex == -1) continue
            index = nextIndex + localeString.length
            if (usedLocaleString != localeString) {
                if (usedLocaleString != null) {
                    // 类似将l_english.yml放到l_simp_chinese目录下的情况，此时直接不作处理
                    return null
                } else {
                    usedLocaleString = localeString
                }
            }
        }
        if (usedLocaleString == null) return null
        val result = mutableSetOf<String>()
        result.add(filePath)
        localeStrings.forEach { result.add(filePath.replace(usedLocaleString, it)) }
        return result
    }

    private fun matchesGameType(context: Context, file: VirtualFile?): Boolean {
        return context.gameType == null || selectGameType(file) == context.gameType
    }

    fun ParadoxFilePathSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        val contextElement = selector.file?.toPsiFile(project)
        return Context(filePath, configExpression, ignoreLocale, contextElement, gameType, project, scope)
    }

    data class Context(
        val filePath: String?,
        val configExpression: CwtDataExpression?,
        val ignoreLocale: Boolean,
        val contextElement: PsiFile?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
