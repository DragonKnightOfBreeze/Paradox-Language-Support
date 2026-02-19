package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher

/**
 * 作用域的代码检查的基类。
 */
abstract class ScopeInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的本地化文件
        return ParadoxPsiFileMatcher.isLocalisationFile(file, injectable = true)
    }
}
