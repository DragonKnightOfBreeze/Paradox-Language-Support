package icu.windea.pls.core

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import icons.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

/**
 * @see icu.windea.pls.core.settings.ParadoxRootDirectoryDescriptor
 */
class ParadoxProjectViewDecorator : ProjectViewNodeDecorator {
    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        if(node is PsiDirectoryNode) {
            val file = node.virtualFile ?: return
            val rootInfo = ParadoxCoreHandler.resolveRootInfo(file) ?: return
            if(rootInfo is ParadoxModRootInfo) {
                //在项目视图中为模组根目录显示特定图标和位置文本（模组的名称和版本信息）
                //忽略存在locationString的情况
                if(data.locationString != null) return
                val icon = when(rootInfo.rootType) {
                    ParadoxRootType.Game -> PlsIcons.GameDirectory
                    ParadoxRootType.Mod -> PlsIcons.ModDirectory
                }
                data.setIcon(icon)
                data.locationString = rootInfo.descriptorInfo.qualifiedName
            }
        }
    }
}