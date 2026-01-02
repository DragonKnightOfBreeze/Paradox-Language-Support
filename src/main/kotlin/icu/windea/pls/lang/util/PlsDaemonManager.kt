package icu.windea.pls.lang.util

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.InlayHintsPassFactoryInternal
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.isFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.FileContentUtilCore
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.ParadoxFileType
import icu.windea.pls.lang.ParadoxModificationTrackers

object PlsDaemonManager {
    // region VFS Methods

    fun isExcludedRootFilePath(rootFilePath: String): Boolean {
        // see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/90
        // exclude some specific root file paths to avoid parsing and indexing unexpected files
        return rootFilePath.isEmpty() || rootFilePath == "/"
    }

    fun findFilesByFileNames(fileNames: Set<String>): Set<VirtualFile> {
        if (fileNames.isEmpty()) return emptySet()
        val files = mutableSetOf<VirtualFile>()
        val projects = ProjectManager.getInstance().openProjects.filter { it.isInitialized && !it.isDisposed }
        val scopes = projects.map { GlobalSearchScope.allScope(it) }
        val scope = scopes.reduceOrNull { a, b -> a.union(b) } ?: return emptySet()
        runReadActionSmartly {
            FilenameIndex.processFilesByNames(fileNames, false, scope, null) { file ->
                if (file.isFile) files.add(file)
                true
            }
        }
        return files
    }

    fun findFilesByRootFilePaths(rootFilePaths: Set<String>): Set<VirtualFile> {
        if (rootFilePaths.isEmpty()) return emptySet()
        val files = mutableSetOf<VirtualFile>()
        runReadActionSmartly {
            rootFilePaths.forEach f@{ rootFilePath ->
                if (isExcludedRootFilePath(rootFilePath)) return@f
                val rootFile = rootFilePath.toVirtualFile() ?: return@f
                VfsUtil.visitChildrenRecursively(rootFile, object : VirtualFileVisitor<Void>() {
                    override fun visitFile(file: VirtualFile): Boolean {
                        if (file.isFile) files.add(file)
                        return true
                    }
                })
            }
        }
        return files
    }

    fun findOpenedFiles(onlyParadoxFiles: Boolean = false, onlyInlineScriptFiles: Boolean = false): Set<VirtualFile> {
        val allEditors = EditorFactory.getInstance().allEditors
        if (allEditors.isEmpty()) return emptySet()
        val files = mutableSetOf<VirtualFile>()
        runReadActionSmartly {
            for (editor in allEditors) {
                val file = editor.virtualFile ?: continue
                if (onlyParadoxFiles && file.fileType !is ParadoxFileType) continue
                if (onlyInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) continue
                files.add(file)
            }
        }
        return files
    }

    // endregion

    // region Refresh Methods

    fun refreshAllFileTrackers() {
        ParadoxModificationTrackers.ScriptFile.incModificationCount()
        ParadoxModificationTrackers.LocalisationFile.incModificationCount()
        ParadoxModificationTrackers.CsvFile.incModificationCount()
        ParadoxModificationTrackers.ScriptFileMap.values.forEach { it.incModificationCount() }
    }

    fun refreshFiles(files: Collection<VirtualFile>, restartAnalyze: Boolean = true, refreshInlayHints: Boolean = true) {
        doRefreshFiles(files, restartAnalyze, refreshInlayHints)
    }

    private fun doRefreshFiles(files: Collection<VirtualFile>, restartAnalyze: Boolean, refreshInlayHints: Boolean) {
        if (files.isEmpty()) return
        val editors = getEditors(files)
        if (editors.isEmpty()) return
        val psiFiles = getEditorPsiFiles(editors)

        // restart DaemonCodeAnalyzer
        if (restartAnalyze) restartAnalyze(psiFiles)
        // refresh inlay hints
        if (refreshInlayHints) refreshInlayHints(editors)
    }

    @Volatile
    private var reparseLock = false // 防止抖动（否则可能出现SOF）

    fun reparseFiles(files: Collection<VirtualFile>, restartAnalyze: Boolean = true, refreshInlayHints: Boolean = true) {
        if (reparseLock) return
        try {
            reparseLock = true
            doReparseFiles(files, restartAnalyze, refreshInlayHints)
        } finally {
            reparseLock = false
        }
    }

    fun doReparseFiles(files: Collection<VirtualFile>, restartAnalyze: Boolean = true, refreshInlayHints: Boolean = true) {
        if (files.isEmpty()) return
        val editors = getEditors(files)
        if (editors.isEmpty()) return
        val psiFiles = getEditorPsiFiles(editors)

        runInEdt {
            // refresh all file trackers
            refreshAllFileTrackers()
            // reparse files
            FileContentUtilCore.reparseFiles(files)

            // restart DaemonCodeAnalyzer
            if (restartAnalyze) restartAnalyze(psiFiles)
            // refresh inlay hints
            if (refreshInlayHints) refreshInlayHints(editors)
        }
    }

    private fun getEditors(files: Collection<VirtualFile>): List<Editor> {
        if (files.isEmpty()) return emptyList()
        val allEditors = EditorFactory.getInstance().allEditors
        return allEditors.filter { editor -> editor.virtualFile.let { it != null && it in files } }
    }

    private fun getEditorPsiFiles(editors: List<Editor>): List<PsiFile> {
        if (editors.isEmpty()) return emptyList()
        return runReadActionSmartly {
            editors.mapNotNull { editor -> editor.virtualFile?.toPsiFile(editor.project!!) }
        }
    }

    private fun restartAnalyze(psiFiles: List<PsiFile>) {
        if (psiFiles.isEmpty()) return
        psiFiles.forEach { psiFile -> DaemonCodeAnalyzer.getInstance(psiFile.project).restart(psiFile) }
    }

    private fun refreshInlayHints(editors: List<Editor>) {
        if (editors.isEmpty()) return
        editors.forEach { editor -> InlayHintsPassFactoryInternal.clearModificationStamp(editor) }
    }
}
