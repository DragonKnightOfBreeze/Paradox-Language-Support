package icu.windea.pls.lang

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.daemon.impl.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*

object PlsManager {
    //region ThreadLocals

    /**
     * 用于标记当前线程是否正在编制索引。（更具体点，是否正在为脚本文件或者本地化文件编制基于文件的索引）
     * @see icu.windea.pls.lang.index.ParadoxFileBasedIndex
     */
    val indexing = ThreadLocal<Boolean>()

    /**
     * 用于标记是否是动态的上下文规则。（例如需要基于脚本上下文）
     */
    val dynamicContextConfigs = ThreadLocal<Boolean>()

    /**
     * 用于标记是否允许不完整的复杂脚本表达式。（用于兼容代码补全）
     */
    val incompleteComplexExpression = ThreadLocal<Boolean>()

    //endregion

    //region Vfs Related Methods

    fun isExcludedRootFilePath(rootFilePath: String): Boolean {
        //see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/90
        //exclude some specific root file paths to avoid parsing and indexing unexpected files
        return rootFilePath.isEmpty() || rootFilePath == "/"
    }

    fun findFilesByFileNames(fileNames: Set<String>): Set<VirtualFile> {
        val files = mutableSetOf<VirtualFile>()
        runReadAction {
            val project = getTheOnlyOpenOrDefaultProject()
            FilenameIndex.processFilesByNames(fileNames, false, GlobalSearchScope.allScope(project), null) { file ->
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
                val project = editor.project ?: continue
                val file = editor.virtualFile ?: continue
                if (onlyParadoxFiles && file.fileType !is ParadoxBaseFileType) continue
                if (onlyInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) continue
                files.add(file)
            }
        }
        return files
    }

    fun reparseAndRefreshFiles(files: Set<VirtualFile>, reparse: Boolean = true, refresh: Boolean = true) {
        if (files.isEmpty()) return
        val allEditors = EditorFactory.getInstance().allEditors
        val editors = allEditors.filter f@{ editor ->
            val project = editor.project ?: return@f false
            val file = editor.virtualFile ?: return@f false
            if (file !in files) return@f false
            true
        }
        val psiFiles = runReadAction {
            editors.mapNotNull { editor -> editor.virtualFile?.toPsiFile(editor.project!!) }
        }
        runInEdt {
            if (reparse) {
                ParadoxModificationTrackers.refreshPsi()
                FileContentUtilCore.reparseFiles(files)
            }

            if (refresh) {
                //refresh code highlighting
                psiFiles.forEach { psiFile -> DaemonCodeAnalyzer.getInstance(psiFile.project).restart(psiFile) }

                //refresh inlay hints
                editors.forEach { editor -> InlayHintsPassFactoryInternal.clearModificationStamp(editor) }
            }
        }
    }

    //endregion
}
