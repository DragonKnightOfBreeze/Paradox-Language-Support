package icu.windea.pls.lang

import com.intellij.ide.util.ModuleRendererFactory
import com.intellij.psi.PsiElement
import com.intellij.util.TextWithIcon
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.qualifiedName

/**
 * 用于在快速文档等处渲染目标的位置信息（规则目录/游戏目录/模组目录）。
 */
class ParadoxModuleRenderFactory : ModuleRendererFactory() {
    override fun rendersLocationString(): Boolean {
        return true
    }

    override fun getModuleTextWithIcon(element: Any?): TextWithIcon? {
        run {
            if (element !is PsiElement || element.language !is CwtLanguage) return@run
            val configGroup = CwtConfigManager.getContainingConfigGroup(element) ?: return@run
            val gameType = configGroup.gameType
            val text = "${gameType.title} Config"
            return TextWithIcon(text, PlsIcons.General.ConfigGroupDirectory)
        }

        val rootFile = selectRootFile(element) ?: return null
        val rootInfo = rootFile.rootInfo ?: return null
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> TextWithIcon(rootInfo.qualifiedName, PlsIcons.General.GameDirectory)
            is ParadoxRootInfo.Mod -> TextWithIcon(rootInfo.qualifiedName, PlsIcons.General.ModDirectory)
            else -> null
        }
    }
}
