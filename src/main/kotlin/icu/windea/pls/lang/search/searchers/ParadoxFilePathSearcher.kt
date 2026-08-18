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
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.CaseInsensitiveStringSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.convertPath
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.ParadoxGameType

/**
 * 文件路径的查询器。
 *
 * @see ParadoxFilePathSearch
 */
@Optimized
class ParadoxFilePathSearcher : QueryExecutorBase<VirtualFile, ParadoxFilePathSearch.Parameters>() {
    // com.intellij.psi.search.FilenameIndex

    override fun processQuery(queryParameters: ParadoxFilePathSearch.Parameters, consumer: Processor<in VirtualFile>) {
        ProgressManager.checkCanceled()
        val context = queryParameters.createContext()
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in VirtualFile>): Boolean {
        if (!context.isValid()) return true
        if (context.configExpression == null) {
            if (context.filePath == null) {
                return processAllFilePaths(context, consumer)
            } else {
                if (context.filePath.isEmpty()) return true
                return processFilePath(context, consumer)
            }
        } else {
            val support = ParadoxPathReferenceExpressionSupport.get(context.configExpression.type) ?: return true
            if (context.filePath == null) {
                return processFilePathWithConfigExpression(context, support, consumer)
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

    private fun processAllFilePaths(context: Context, consumer: Processor<in VirtualFile>): Boolean {
        val indexId = ChronicleIndexKeys.FilePath
        val keys = FileBasedIndex.getInstance().getAllKeys(indexId, context.project)
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(indexId, keys, context.scope, null, null) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
            if (!matchesGameType(context, file)) return@p true // check game type at file level
            consumer.process(file)
        }
    }

    private fun processFilePath(context: Context, consumer: Processor<in VirtualFile>): Boolean {
        // 3.0.2 can be case-insensitive and/or locale-insensitive here
        val filePath = context.filePath ?: return true
        val indexId = ChronicleIndexKeys.FilePath
        val filePaths = getFilePaths(context, filePath)
        if (filePaths.isEmpty()) return true
        val keys = mutableSetOf<String>()
        if (context.ignoreCase || context.ignoreExtension) {
            val expected = if (context.ignoreCase) CaseInsensitiveStringSet().apply { addAll(filePaths) } else filePaths
            FileBasedIndex.getInstance().processAllKeys(indexId, p@{ p ->
                val actual = if (context.ignoreExtension) p.convertPath { b, _ -> b } else p
                if (!expected.contains(actual)) return@p true
                keys.add(p)
                true // continue processing
            }, context.scope, null)
        } else {
            keys.addAll(filePaths)
        }
        if (keys.isEmpty()) return true
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(indexId, keys, context.scope, null, null) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
            if (!matchesGameType(context, file)) return@p true // check game type at file level
            consumer.process(file)
        }
    }

    private fun processFilePathWithConfigExpression(context: Context, support: ParadoxPathReferenceExpressionSupport, consumer: Processor<in VirtualFile>): Boolean {
        // 3.0.2 should be case-sensitive here, should be enough
        val configExpression = context.configExpression ?: return true
        val indexId = ChronicleIndexKeys.FilePath
        val keys = mutableSetOf<String>()
        FileBasedIndex.getInstance().processAllKeys(indexId, p@{ p ->
            if (!support.matches(configExpression, context.contextElement, p)) return@p true
            keys.add(p)
            true // continue processing
        }, context.scope, null)
        if (keys.isEmpty()) return true
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(indexId, keys, context.scope, null, null) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
            if (!matchesGameType(context, file)) return@p true // check game type at file level
            consumer.process(file)
        }
    }

    private fun processResolvedPaths(context: Context, resolved: Set<String>, consumer: Processor<in VirtualFile>): Boolean {
        // 3.0.2 should be case-sensitive here, should be enough
        if (resolved.isEmpty()) return true
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(ChronicleIndexKeys.FilePath, resolved, context.scope, null, null) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxAnalysisManager.getFileInfo(file) ?: return@p true // ensure file info is resolved here
            if (!matchesGameType(context, file)) return@p true // check game type at file level
            consumer.process(file)
        }
    }

    private fun processResolvedFileNames(context: Context, resolved: Set<String>, support: ParadoxPathReferenceExpressionSupport, consumer: Processor<in VirtualFile>): Boolean {
        // 3.0.2 should be case-sensitive here, should be enough
        if (resolved.isEmpty()) return true
        val configExpression = context.configExpression ?: return true
        val resolvedFiles = sortedSetOf<VirtualFile>(compareBy { it.path })
        FilenameIndex.processFilesByNames(resolved, true, context.scope, null) p@{ file ->
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
        val configGroup = ChronicleFacade.getConfigGroup()
        val globalLocales = ParadoxLocaleManager.getGlobalLocales(configGroup)
        val localeStrings = globalLocales.mapFast { it.shortId }
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
        localeStrings.forEachFast { result.add(filePath.replace(usedLocaleString, it)) }
        return result
    }

    private fun matchesGameType(context: Context, file: VirtualFile?): Boolean {
        return context.gameType == null || selectGameType(file) == context.gameType
    }

    fun ParadoxFilePathSearch.Parameters.createContext(): Context {
        val contextElement = selector.file?.toPsiFile(project)
        return Context(filePath, configExpression, ignoreCase, ignoreExtension, ignoreLocale, contextElement, gameType, project, scope)
    }

    data class Context(
        val filePath: String?,
        val configExpression: CwtDataExpression?,
        val ignoreCase: Boolean,
        val ignoreExtension: Boolean,
        val ignoreLocale: Boolean,
        val contextElement: PsiFile?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
