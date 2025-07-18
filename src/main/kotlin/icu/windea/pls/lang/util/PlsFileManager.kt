package icu.windea.pls.lang.util

import com.intellij.injected.editor.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.vfs.*
import com.intellij.testFramework.*
import com.intellij.util.*
import icu.windea.pls.core.util.Processors
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.actions.*

object PlsFileManager {
    fun isLightFile(file: VirtualFile): Boolean {
        return file is LightVirtualFileBase
    }

    fun isInjectedFile(file: VirtualFile): Boolean {
        return file is VirtualFileWindow
    }

    fun findFiles(e: AnActionEvent): Set<VirtualFile> {
        val editor = e.editor
        if (editor != null) {
            val file = e.getData(LangDataKeys.VIRTUAL_FILE)
            return file.singleton().setOrEmpty()
        }
        val files = e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY)
        if (files.isNullOrEmpty()) return emptySet()
        return files.toSet()
    }

    fun findFiles(e: AnActionEvent, deep: Boolean = false, filter: (VirtualFile) -> Boolean): Set<VirtualFile> {
        val processor = Processors.collect(mutableSetOf(), filter)
        processFiles(e, deep, processor)
        return processor.collection
    }

    fun processFiles(e: AnActionEvent, deep: Boolean = false, processor: Processor<VirtualFile>): Boolean {
        val editor = e.editor
        if (editor != null) {
            val file = e.getData(LangDataKeys.VIRTUAL_FILE)
            if (file == null) return true
            return processor.process(file)
        }
        val files = e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY)
        if (files.isNullOrEmpty()) return true
        return files.all { file ->
            if (deep && file.isDirectory) {
                VfsUtil.processFilesRecursively(file, processor)
            } else {
                processor.process(file)
            }
        }
    }
}
