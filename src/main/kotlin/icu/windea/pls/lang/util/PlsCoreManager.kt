package icu.windea.pls.lang.util

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.daemon.impl.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*

object PlsCoreManager {
    //region ThreadLocals

    /**
     * 用于标记当前线程是否正在为[ParadoxMergedIndex]编制索引。
     */
    val processMergedIndex = ThreadLocal<Boolean>()

    /**
     * 用于标记当前线程是否正在为[ParadoxMergedIndex]编制索引并且正在解析引用。
     */
    val resolveForMergedIndex = ThreadLocal<Boolean>()

    /**
     * 用于标记是否是动态的上下文规则。（例如需要基于脚本上下文）
     */
    val dynamicContextConfigs = ThreadLocal<Boolean>()

    /**
     * 用于标记是否允许不完整的复杂脚本表达式。（用于兼容代码补全）
     */
    val incompleteComplexExpression = ThreadLocal<Boolean>()

    //endregion

    //region VFS Methods

    fun isExcludedRootFilePath(rootFilePath: String): Boolean {
        //see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/90
        //exclude some specific root file paths to avoid parsing and indexing unexpected files
        return rootFilePath.isEmpty() || rootFilePath == "/"
    }

    fun findFilesByFileNames(fileNames: Set<String>): Set<VirtualFile> {
        val files = mutableSetOf<VirtualFile>()
        val projects = ProjectManager.getInstance().openProjects.filter { it.isInitialized && !it.isDisposed }
        val scopes = projects.map { GlobalSearchScope.allScope(it) }
        val scope = scopes.reduceOrNull { a, b -> a.union(b) } ?: return emptySet()
        runReadAction {
            FilenameIndex.processFilesByNames(fileNames, false, scope, null) { file ->
                if (file.isFile) files.add(file)
                true
            }
        }
        return files
    }

    fun findFilesByRootFilePaths(rootFilePaths: Set<String>): MutableSet<VirtualFile> {
        val files = mutableSetOf<VirtualFile>()
        runReadAction {
            rootFilePaths.forEach f@{ rootFilePath ->
                if (isExcludedRootFilePath(rootFilePath)) return@f
                val rootFile = VfsUtil.findFile(rootFilePath.toPathOrNull() ?: return@f, true) ?: return@f
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
        val files = mutableSetOf<VirtualFile>()
        runReadAction {
            val allEditors = EditorFactory.getInstance().allEditors
            for (editor in allEditors) {
                val file = editor.virtualFile ?: continue
                if (onlyParadoxFiles && file.fileType !is ParadoxBaseFileType) continue
                if (onlyInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) continue
                files.add(file)
            }
        }
        return files
    }

    //endregion

    //region VFS Refresh Methods

    @Volatile
    private var reparseLock = false //防止抖动（否则可能出现SOF）

    fun reparseFiles(files: Collection<VirtualFile>) {
        if (reparseLock) return
        try {
            reparseLock = true
            doReparseFiles(files)
        } finally {
            reparseLock = false
        }
    }

    fun doReparseFiles(files: Collection<VirtualFile>) {
        if (files.isEmpty()) return
        val allEditors = EditorFactory.getInstance().allEditors
        val editors = allEditors.filter f@{ editor ->
            val file = editor.virtualFile ?: return@f false
            if (file !in files) return@f false
            true
        }
        val psiFiles = runReadAction {
            editors.mapNotNull { editor -> editor.virtualFile?.toPsiFile(editor.project!!) }
        }
        runInEdt {
            ParadoxModificationTrackers.refreshPsi()
            FileContentUtilCore.reparseFiles(files)

            //restart DaemonCodeAnalyzer
            psiFiles.forEach { psiFile -> DaemonCodeAnalyzer.getInstance(psiFile.project).restart(psiFile) }
            //refresh inlay hints
            editors.forEach { editor -> InlayHintsPassFactoryInternal.clearModificationStamp(editor) }
        }
    }

    fun refreshFiles(files: Collection<VirtualFile>, restartDaemon: Boolean = true, refreshInlayHints: Boolean = true) {
        doRefreshFiles(files, restartDaemon, refreshInlayHints)
    }

    private fun doRefreshFiles(files: Collection<VirtualFile>, restartDaemon: Boolean, refreshInlayHints: Boolean) {
        if (files.isEmpty()) return
        val allEditors = EditorFactory.getInstance().allEditors
        val editors = allEditors.filter f@{ editor ->
            val file = editor.virtualFile ?: return@f false
            if (file !in files) return@f false
            true
        }
        if (editors.isEmpty()) return
        val psiFiles = runReadAction {
            editors.mapNotNull { editor -> editor.virtualFile?.toPsiFile(editor.project!!) }
        }

        if (restartDaemon) {
            //restart DaemonCodeAnalyzer
            psiFiles.forEach { psiFile -> DaemonCodeAnalyzer.getInstance(psiFile.project).restart(psiFile) }
        }
        if (refreshInlayHints) {
            //refresh inlay hints
            editors.forEach { editor -> InlayHintsPassFactoryInternal.clearModificationStamp(editor) }
        }
    }

    //目前并未用到 - 当图片发生更改时，不自动刷新所有可能用来渲染图片的内嵌提示
    //@Suppress("UnstableApiUsage")
    //suspend fun refreshInlayHintsImagesChangedIfNecessary() {
    //    val settings = InlayHintsSettings.instance()
    //    val enabledScriptProviders = InlayHintsProviderExtension.allForLanguage(ParadoxScriptLanguage)
    //        .filter { it is ParadoxScriptHintsProvider<*> && it.renderIcon }
    //        .filter { settings.hintsEnabled(it.key, ParadoxScriptLanguage) }
    //    val enabledLocalisationProviders = InlayHintsProviderExtension.allForLanguage(ParadoxLocalisationLanguage)
    //        .filter { it is ParadoxLocalisationHintsProvider<*> && it.renderIcon }
    //        .filter { settings.hintsEnabled(it.key, ParadoxLocalisationLanguage) }
    //    val refreshScriptFile = enabledScriptProviders.isNotEmpty()
    //    val refreshLocalisationFile = enabledLocalisationProviders.isNotEmpty()
    //    if (!refreshScriptFile && !refreshLocalisationFile) return
    //
    //    val allEditors = EditorFactory.getInstance().allEditors
    //    val editors = allEditors.filter f@{ editor ->
    //        val file = editor.virtualFile ?: return@f false
    //        when (file.fileType) {
    //            ParadoxScriptFileType -> refreshScriptFile
    //            ParadoxLocalisationFileType -> refreshLocalisationFile
    //            else -> false
    //        }
    //    }
    //    if (editors.isEmpty()) return
    //
    //    withContext(Dispatchers.UI) {
    //        editors.forEach { editor -> InlayHintsPassFactoryInternal.clearModificationStamp(editor) }
    //    }
    //}

    //endregion
}
