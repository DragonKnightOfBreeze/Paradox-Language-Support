package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import icons.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

/**
 * 在项目视图中为游戏或模组目录提供特定的图标和额外的信息文本。
 */
class ParadoxProjectViewDecorator : ProjectViewNodeDecorator {
    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        if (node is PsiDirectoryNode) {
            val file = node.virtualFile ?: return
            val rootInfo = file.rootInfo ?: return
            if (data.locationString != null) return //忽略存在locationString的情况
            val icon = when (rootInfo) {
                is ParadoxGameRootInfo -> PlsIcons.GameDirectory
                is ParadoxModRootInfo -> PlsIcons.ModDirectory
            }
            data.setIcon(icon)
            data.locationString = rootInfo.qualifiedName
        }
    }
}
