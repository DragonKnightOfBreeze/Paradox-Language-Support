package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.util.Processors
import icu.windea.pls.lang.actions.editor

object PlsFileManipulator {
    fun buildSequence(e: AnActionEvent, deep: Boolean = false): Sequence<VirtualFile> {
        val editor = e.editor
        if (editor != null) {
            val file = e.getData(LangDataKeys.VIRTUAL_FILE)
            if (file == null) return emptySequence()
            return sequenceOf(file)
        }
        val files = e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY)
        if (files.isNullOrEmpty()) return emptySequence()
        if (!deep) return files.asSequence()
        return sequence {
            files.forEach { file ->
                if (deep && file.isDirectory) {
                    // NOTE 这里或许存在更好的实现方式，但是目前就这样吧
                    val processor = Processors.collect(mutableSetOf<VirtualFile>())
                    VfsUtil.processFilesRecursively(file, processor)
                    yieldAll(processor.collection)
                } else {
                    yield(file)
                }
            }
        }
    }
}
