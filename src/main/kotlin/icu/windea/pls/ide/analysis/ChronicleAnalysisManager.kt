package icu.windea.pls.ide.analysis

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.InlayHintsPassFactoryInternal
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.isFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.FileContentUtilCore
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.ParadoxFileType
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.analysis.ParadoxAnalysisDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ChronicleAnalysisManager {
    fun isExcludedRootFilePath(rootFilePath: String): Boolean {
        // https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/90
        // exclude some specific root file paths to avoid parsing and indexing unexpected files
        return rootFilePath.isEmpty() || rootFilePath == "/"
    }

    fun findAllOpenFiles(): Set<VirtualFile> {
        val allEditors = EditorFactory.getInstance().allEditors
        if (allEditors.isEmpty()) return emptySet()
        val files = mutableSetOf<VirtualFile>()
        runSmartReadAction {
            for (editor in allEditors) {
                val file = editor.virtualFile ?: continue
                if (!file.isFile || file.fileType !is ParadoxFileType) continue
                files.add(file)
            }
        }
        if (files.isEmpty()) return emptySet()
        return files
    }

    fun findAllFilesByFileNames(fileNames: Set<String>): Set<VirtualFile> {
        if (fileNames.isEmpty()) return emptySet()
        val files = mutableSetOf<VirtualFile>()
        val projects = ProjectManager.getInstance().openProjects.filter { it.isInitialized && !it.isDisposed }
        val scopes = projects.map { GlobalSearchScope.allScope(it) }
        val scope = scopes.reduceOrNull { a, b -> a.union(b) } ?: return emptySet()
        runSmartReadAction {
            FilenameIndex.processFilesByNames(fileNames, false, scope, null) { file ->
                if (file.isFile && file.fileType is ParadoxFileType) files.add(file)
                true
            }
        }
        if (files.isEmpty()) return emptySet()
        return files
    }

    fun findAllFilesByRootFilePaths(rootFilePaths: Set<String>): Set<VirtualFile> {
        if (rootFilePaths.isEmpty()) return emptySet()
        val files = mutableSetOf<VirtualFile>()
        runSmartReadAction {
            for (rootFilePath in rootFilePaths) {
                if (isExcludedRootFilePath(rootFilePath)) continue
                val rootFile = rootFilePath.toVirtualFile() ?: continue
                VfsUtil.visitChildrenRecursively(rootFile, object : VirtualFileVisitor<Void>() {
                    override fun visitFile(file: VirtualFile): Boolean {
                        if (file.isFile && file.fileType is ParadoxFileType) files.add(file)
                        return true
                    }
                })
            }
        }
        if (files.isEmpty()) return emptySet()
        return files
    }

    fun findRootFilesByRootFilePaths(rootFilePaths: Set<String>): Set<VirtualFile> {
        if (rootFilePaths.isEmpty()) return emptySet()
        val files = mutableSetOf<VirtualFile>()
        runSmartReadAction {
            for (rootFilePath in rootFilePaths) {
                if (isExcludedRootFilePath(rootFilePath)) continue
                val rootFile = rootFilePath.toVirtualFile() ?: continue
                files.add(rootFile)
            }
        }
        if (files.isEmpty()) return emptySet()
        return files
    }

    /**
     * 刷新解析数据（目前仅会清空 rootInfo）。
     */
    fun refreshAnalysisData(rootFiles: Collection<VirtualFile>) {
        if (rootFiles.isEmpty()) return
        with(ParadoxAnalysisDataService.getInstance()) {
            rootFiles.forEach { rootFile ->
                rootFile.cachedRootInfo = null
            }
        }
    }

    /**
     * 刷新 [ParadoxModificationTrackers] 中的所有文件更改追踪器。
     */
    fun refreshFileModificationTrackers() {
        ParadoxModificationTrackers.ScriptFile.incModificationCount()
        ParadoxModificationTrackers.LocalisationFile.incModificationCount()
        ParadoxModificationTrackers.CsvFile.incModificationCount()
        ParadoxModificationTrackers.ScriptFileMap.values.forEach { it.incModificationCount() }
    }

    /**
     * 刷新所有已打开的文件的高亮（仅限文件类型属于 [ParadoxFileType] 的文件）。注意这不会同时刷新内嵌提示。
     */
    fun refreshFiles() {
        val allEditors = EditorFactory.getInstance().allEditors
        if (allEditors.isEmpty()) return
        val coroutineScope = ChronicleFacade.getCoroutineScope()
        coroutineScope.launch {
            readAction {
                for (editor in allEditors) {
                    val project = editor.project ?: continue
                    val file = editor.virtualFile ?: continue
                    if (!file.isFile || file.fileType !is ParadoxFileType) continue
                    val psiFile = file.toPsiFile(project) ?: continue
                    // NOTE 3.0.0 [compatibility] `DaemonCodeAnalyzer.restart(PsiFile)` is deprecated since IDEA-253
                    //  - Use `DaemonCodeAnalyzer.restart(PsiFile, Object)` instead
                    DaemonCodeAnalyzer.getInstance(project).restart(psiFile, "ChronicleAnalysisManager.refreshFiles")
                }
            }
        }
    }

    /**
     * 刷新所有已打开的文件的内嵌提示（仅限文件类型属于 [ParadoxFileType] 的文件）。
     */
    fun refreshInlayHints() {
        val allEditors = EditorFactory.getInstance().allEditors
        if (allEditors.isEmpty()) return
        for (editor in allEditors) {
            val file = editor.virtualFile ?: continue
            if (!file.isFile || file.fileType !is ParadoxFileType) continue
            InlayHintsPassFactoryInternal.clearModificationStamp(editor)
        }
    }

    /**
     * 重新解析指定的一组文件（重建语法树，之后会自动重建索引、刷新高亮和内嵌提示）。
     */
    fun reparseFiles(files: Collection<VirtualFile>) {
        if (files.isEmpty()) return
        val coroutineScope = ChronicleFacade.getCoroutineScope()
        coroutineScope.launch {
            // refresh file trackers
            refreshFileModificationTrackers()
            // reparse files
            withContext(Dispatchers.EDT) {
                FileContentUtilCore.reparseFiles(files)
            }
        }
    }
}
