package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 事件的代码检查的基类。
 */
abstract class EventInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 仅检查事件的脚本文件
        if (file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path
        return filePath.path.matchesAntPattern("events/**/*.txt")
    }
}
