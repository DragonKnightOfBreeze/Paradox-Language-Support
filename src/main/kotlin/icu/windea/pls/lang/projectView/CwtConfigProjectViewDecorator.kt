package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.SyntheticLibraryElementNode
import icu.windea.pls.PlsIcons
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider

/**
 * 在项目视图中：
 * - 为规则分组目录提供特殊图标和额外的信息文本。
 */
class CwtConfigProjectViewDecorator : ProjectViewNodeDecorator {
    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        forConfigGroupDirectory(node, data)
    }

    private fun forConfigGroupDirectory(node: ProjectViewNode<*>, data: PresentationData) {
        if (node !is PsiDirectoryNode) return
        val file = node.virtualFile ?: return
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val fileProvider = fileProviders.find { it.getRootDirectory(node.project) == file } ?: return

        // 特殊图标
        data.setIcon(PlsIcons.General.ConfigGroupDirectory)

        // 来自 EP 实现的提示文本
        run {
            if (data.locationString != null) return@run
            if (node.parent !is SyntheticLibraryElementNode) return@run
            val hintMessage = fileProvider.getHintMessage()
            if (hintMessage.isNullOrEmpty()) return@run
            data.locationString = hintMessage
        }
    }
}
