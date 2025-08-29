package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.qualifiedName

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
                is ParadoxRootInfo.Game -> PlsIcons.General.GameDirectory
                is ParadoxRootInfo.Mod -> PlsIcons.General.ModDirectory
                else -> return
            }
            data.setIcon(icon)
            data.locationString = rootInfo.qualifiedName
        }
    }
}
