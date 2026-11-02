package icu.windea.pls.lang

import com.intellij.ide.util.ModuleRendererFactory
import com.intellij.psi.PsiElement
import com.intellij.util.TextWithIcon
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.configGroup.CwtConfigFileService
import icu.windea.pls.cwt.CwtLanguage

/**
 * 在快速文档等处：
 * - 渲染规则文件的位置信息（游戏类型）。
 */
class CwtConfigModuleRenderFactory : ModuleRendererFactory() {
    override fun rendersLocationString(): Boolean {
        return true
    }

    override fun getModuleTextWithIcon(element: Any?): TextWithIcon? {
        return forConfig(element)
    }

    private fun forConfig(element: Any?): TextWithIcon? {
        if (element !is PsiElement || element.language !== CwtLanguage) return null
        val file = element.containingFile ?: return null
        val configGroup = CwtConfigFileService.getInstance().getContainingConfigGroup(file) ?: return null
        val gameType = configGroup.gameType
        val text = "${gameType.title} Config"
        return TextWithIcon(text, ChronicleIcons.General.ConfigGroupDirectory)
    }
}
