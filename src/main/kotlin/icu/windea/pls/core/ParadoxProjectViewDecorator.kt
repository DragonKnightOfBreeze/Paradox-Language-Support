package icu.windea.pls.core

import com.intellij.icons.*
import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*

/**
 * @see icu.windea.pls.core.ui.ParadoxRootDirectoryDescriptor
 */
class ParadoxProjectViewDecorator : ProjectViewNodeDecorator {
    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        if(node is PsiFileNode) {
            val file = node.virtualFile ?: return
            if(!file.isDirectory) return
            val fileInfo = file.fileInfo ?: return
            val rootInfo = fileInfo.rootInfo
            if(rootInfo is ParadoxModRootInfo && file == rootInfo.rootFile) {
                //在项目视图中为模组根目录显示模组的名称和版本信息
                //忽略使用特殊的icon或者存在locationString的情况，如项目根目录
                if(data.getIcon(true) != AllIcons.Nodes.Folder) return
                if(data.locationString != null) return
                val icon = when(rootInfo.rootType) {
                    ParadoxRootType.Game -> PlsIcons.GameDirectory
                    ParadoxRootType.Mod -> PlsIcons.ModDirectory
                }
                data.setIcon(icon)
                val name = rootInfo.descriptorInfo.name
                val version = rootInfo.descriptorInfo.version
                val comment = buildString {
                    append(name)
                    if(version != null) append("@").append(version)
                }
                data.locationString = comment
            }
        }
    }
}