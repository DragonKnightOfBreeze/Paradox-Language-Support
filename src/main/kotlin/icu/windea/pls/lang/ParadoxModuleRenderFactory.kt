package icu.windea.pls.lang

import com.intellij.ide.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.util.*
import icu.windea.pls.cwt.*
import icu.windea.pls.model.*

/**
 * 用于在快速文档等处渲染目标的位置信息（规则目录/游戏目录/模组目录）。
 */
class ParadoxModuleRenderFactory : ModuleRendererFactory() {
    override fun rendersLocationString(): Boolean {
        return true
    }

    override fun getModuleTextWithIcon(element: Any?): TextWithIcon? {
        run {
            if (element !is PsiElement || element.language != CwtLanguage) return@run
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
        }
    }
}
