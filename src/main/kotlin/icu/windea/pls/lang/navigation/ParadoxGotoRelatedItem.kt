package icu.windea.pls.lang.navigation

import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.util.NlsContexts.Separator
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.qualifiedName
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxGotoRelatedItem(element: PsiElement, @Separator group: String) : GotoRelatedItem(element, group) {
    override fun getCustomName(): String? {
        val element = element
        if (element is ParadoxScriptProperty) {
            val definitionInfo = element.definitionInfo
            if (definitionInfo != null) return definitionInfo.name.or.anonymous()
        }
        return null
    }

    override fun getCustomContainerName(): String? {
        //使用相对于游戏或模组目录的路径，并且带上游戏信息/模组信息，或者使用虚拟文件的绝对路径
        val element = element
        if (element == null) return null
        val file = element.containingFile
        val fileInfo = file.fileInfo ?: return file.virtualFile.path
        val path = fileInfo.path.path
        val qualifiedName = fileInfo.rootInfo.qualifiedName
        return PlsBundle.message("paradox.goto.related.item.container.name", path, qualifiedName)
    }

    companion object {
        fun createItems(elements: Collection<PsiElement>, @Separator group: String): List<ParadoxGotoRelatedItem> {
            return elements.mapTo(mutableListOf()) { ParadoxGotoRelatedItem(it, group) }
        }
    }
}
