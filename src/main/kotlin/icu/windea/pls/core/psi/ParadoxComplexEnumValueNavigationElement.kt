package icu.windea.pls.core.psi

import com.intellij.navigation.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 用于在双击shift进行快速查找时显示正确的图标和名字。
 */
class ParadoxComplexEnumValueNavigationElement(
    element: ParadoxScriptStringExpressionElement
): ParadoxFakePsiElement(element) {
    val _name = ParadoxComplexEnumValueHandler.getName(element)
    
    override fun getIcon(): Icon {
        return PlsIcons.ComplexEnumValue
    }
    
    override fun getName(): String? {
        return _name
    }
    
    override fun getTypeName(): String {
        return PlsBundle.message("script.description.complexEnumValue")
    }
    
    override fun getText(): String? {
        return (parent as ParadoxScriptStringExpressionElement).text
    }
    
    override fun getNameIdentifier(): PsiElement? {
        return this
    }
    
    override fun getPresentation(): ItemPresentation? {
        return (parent as ParadoxScriptStringExpressionElement).presentation
    }
    
    override fun getNavigationElement(): PsiElement {
        return parent
    }
}