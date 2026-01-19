package icu.windea.pls.lang

import com.intellij.ide.util.ModuleRendererFactory
import com.intellij.util.TextWithIcon
import icu.windea.pls.PlsIcons
import icu.windea.pls.model.ParadoxRootInfo

/**
 * 在快速文档等处：
 * - 渲染游戏或模组文件的位置信息（名字 & 版本）。
 */
class ParadoxModuleRenderFactory : ModuleRendererFactory() {
    override fun rendersLocationString(): Boolean {
        return true
    }

    override fun getModuleTextWithIcon(element: Any?): TextWithIcon? {
        return forGameOrMod(element)
    }

    private fun forGameOrMod(element: Any?): TextWithIcon? {
        val rootFile = selectRootFile(element) ?: return null
        val rootInfo = rootFile.rootInfo ?: return null
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val icon = when (rootInfo) {
            is ParadoxRootInfo.Game -> PlsIcons.General.GameDirectory
            is ParadoxRootInfo.Mod -> PlsIcons.General.ModDirectory
        }
        return TextWithIcon(rootInfo.qualifiedName, icon)
    }
}
