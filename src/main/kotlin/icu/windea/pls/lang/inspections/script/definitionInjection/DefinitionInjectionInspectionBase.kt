package icu.windea.pls.lang.inspections.script.definitionInjection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.model.constraints.ParadoxPathConstraint

/**
 * 定义注入的代码检查的基类。
 */
abstract class DefinitionInjectionInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求受游戏类型支持
        val gameType = selectGameType(file)
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件（可接受定义注入）
        return ParadoxPsiFileMatchService.isScriptFile(file, ParadoxPathConstraint.AcceptDefinitionInjection)
    }
}
