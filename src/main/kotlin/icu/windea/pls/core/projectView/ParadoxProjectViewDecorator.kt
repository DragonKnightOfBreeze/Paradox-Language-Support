package icu.windea.pls.core.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

/**
 * 在项目视图中为游戏或模组根目录提供特定的图标和额外的信息文本。
 */
class ParadoxProjectViewDecorator : ProjectViewNodeDecorator {
    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        if(node is PsiDirectoryNode) {
            val file = node.virtualFile ?: return
            val rootInfo = file.rootInfo ?: return
            //在项目视图中为模组或游戏根目录显示特定图标和位置文本（模组的名称和版本信息）
            //忽略存在locationString的情况
            if(data.locationString != null) return
            when(rootInfo) {
                is ParadoxGameRootInfo -> {
                    data.setIcon(PlsIcons.GameDirectory)
                    data.locationString = rootInfo.qualifiedName
                }
                is ParadoxModRootInfo -> {
                    data.setIcon(PlsIcons.ModDirectory)
                    data.locationString = rootInfo.qualifiedName
                }
            }
        }
    }
}