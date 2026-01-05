package icu.windea.pls.lang.inspections.script.definitionInjection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager

/**
 * 定义注入的代码检查的基类。
 */
abstract class DefinitionInjectionInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val gameType = selectGameType(file)
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return false

        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = true)
    }
}
