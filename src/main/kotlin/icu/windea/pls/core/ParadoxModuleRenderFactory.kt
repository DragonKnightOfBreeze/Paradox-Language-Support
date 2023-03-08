package icu.windea.pls.core

import com.intellij.ide.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*

/**
 * 用于渲染定义、本地化等所在的位置（游戏目录/模组目录）。在弹出项右侧显示特定的图标和位置文本。
 */
class ParadoxModuleRenderFactory : ModuleRendererFactory() {
    override fun rendersLocationString(): Boolean {
        return true
    }
    
    override fun getModuleTextWithIcon(element: Any?): TextWithIcon? {
        val file = selectFile(element) ?: return null
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        return when(rootInfo) {
            is ParadoxGameRootInfo -> {
                TextWithIcon(rootInfo.qualifiedName, PlsIcons.GameDirectory)
            }
            is ParadoxModRootInfo -> {
                TextWithIcon(rootInfo.qualifiedName, PlsIcons.ModDirectory)
            }
        }
    }
}