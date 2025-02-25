package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.ep.configGroup.*

/**
 * 在项目视图中为规则分组所在的目录提供特定的图标和额外的信息文本。
 */
class CwtConfigProjectViewDecorator : ProjectViewNodeDecorator {
    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        if (node is PsiDirectoryNode) {
            val file = node.virtualFile ?: return
            val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
            val fileProvider = fileProviders.find { it.getRootDirectory(node.project) == file } ?: return
            if (data.locationString != null) return //忽略存在locationString的情况
            data.setIcon(PlsIcons.ConfigGroupDirectory)
            if (node.parent is SyntheticLibraryElementNode) {
                data.locationString = fileProvider.getHintMessage()
            }
        }
    }
}
