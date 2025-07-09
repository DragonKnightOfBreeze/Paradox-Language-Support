package icu.windea.pls.lang.util

import com.intellij.injected.editor.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.vfs.*
import com.intellij.testFramework.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.actions.*

object PlsFileManager {
    fun isLightFile(file: VirtualFile): Boolean {
        return file is LightVirtualFileBase
    }

    fun isInjectedFile(file: VirtualFile): Boolean {
        return file is VirtualFileWindow
    }

    fun collectFiles(e: AnActionEvent): Set<VirtualFile> {
        val editor = e.editor
        if (editor != null) {
            val file = e.getData(LangDataKeys.VIRTUAL_FILE)
            return file.toSingletonSetOrEmpty()
        }
        val files = e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY)
        if (files.isNullOrEmpty()) return emptySet()
        return files.toSet()
    }

    fun collectFiles(e: AnActionEvent, deep: Boolean = false, filter: (VirtualFile) -> Boolean): Set<VirtualFile> {
        val files = collectFiles(e)
        val result = mutableSetOf<VirtualFile>()
        for (file in files) {
            if (deep && file.isDirectory) {
                VfsUtil.visitChildrenRecursively(file, object : VirtualFileVisitor<Void>() {
                    override fun visitFile(file: VirtualFile): Boolean {
                        if (filter(file)) result.add(file)
                        return true
                    }
                })
            } else {
                if (filter(file)) result.add(file)
            }
        }
        return result
    }
}
