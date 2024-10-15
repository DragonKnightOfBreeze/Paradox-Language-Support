package icu.windea.pls.lang.psi

import com.intellij.navigation.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 用于在双击shift进行快速查找时显示正确的图标和名字。
 */
class ParadoxDefinitionNavigationElement(
    element: ParadoxScriptDefinitionElement,
    private val definitionInfo: ParadoxDefinitionInfo
) : ParadoxFakePsiElement(element) {
    override fun getIcon(): Icon {
        return PlsIcons.Nodes.Definition(definitionInfo.type)
    }

    override fun getName(): String? {
        return definitionInfo.name.orNull()
    }

    override fun getTypeName(): String {
        return PlsBundle.message("script.description.definition")
    }

    override fun getText(): String? {
        return (parent as ParadoxScriptDefinitionElement).text
    }

    override fun getNameIdentifier(): PsiElement? {
        return (parent as ParadoxScriptDefinitionElement).nameIdentifier
    }

    override fun getPresentation(): ItemPresentation? {
        return (parent as ParadoxScriptDefinitionElement).presentation
    }

    override fun getNavigationElement(): PsiElement {
        return parent
    }
}
