package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 在项目视图中：
 * - 为游戏或模组目录提供特殊图标和额外的信息文本。
 * - 为入口目录提供特殊图标。
 * - 为文件定义提供特殊图标，并处理展示文本。
 */
class ParadoxProjectViewDecorator : ProjectViewNodeDecorator {
    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        forGameOrModDirectory(node, data)
            || forEntryDirectory(node, data)
            || forFileDefinition(node, data)
    }

    private fun forGameOrModDirectory(node: ProjectViewNode<*>, data: PresentationData): Boolean {
        if (node !is PsiDirectoryNode) return false
        val file = node.virtualFile ?: return false
        val rootInfo = file.rootInfo ?: return false

        // 特殊图标
        run {
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return@run
            val icon = when (rootInfo) {
                is ParadoxRootInfo.Game -> PlsIcons.General.GameDirectory
                is ParadoxRootInfo.Mod -> PlsIcons.General.ModDirectory
            }
            data.setIcon(icon)
        }

        // 名字 & 版本
        run {
            if (data.locationString != null) return@run
            data.locationString = rootInfo.qualifiedName
        }

        return true
    }

    private fun forEntryDirectory(node: ProjectViewNode<*>, data: PresentationData): Boolean {
        if (node !is PsiDirectoryNode) return false
        val file = node.element?.value ?: return false
        val fileInfo = file.fileInfo ?: return false
        if (fileInfo.path.isNotEmpty()) return false

        // 特殊图标
        data.setIcon(PlsIcons.General.EntryDirectory)

        return true
    }

    private fun forFileDefinition(node: ProjectViewNode<*>, data: PresentationData): Boolean {
        if (node !is PsiFileNode) return false
        val file = node.element?.value ?: return false
        if (file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo ?: return false
        if (fileInfo.group == ParadoxFileGroup.ModDescriptor) return false // 排除模组描述符文件
        val definitionInfo = file.definitionInfo ?: return false

        // 特殊图标
        data.setIcon(PlsIcons.Nodes.Definition(definitionInfo.type))

        // 定义名（通常等同于去掉扩展名后的文件名）
        data.presentableText = definitionInfo.name

        return true
    }
}
