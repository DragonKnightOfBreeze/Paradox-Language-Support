package icu.windea.pls.core.inspections

import com.intellij.codeInspection.*
import com.intellij.codeInspection.lang.*
import com.intellij.codeInspection.reference.*
import com.intellij.psi.*

/**
 * 这个扩展的目的是让代码检查页面中的按目录分组选项能够按照相对于游戏或模组根目录的路径分组，而非简单地按照目录名分组。
 */
class ParadoxInspectionExtensionsFactory: InspectionExtensionsFactory() {
    override fun createGlobalInspectionContextExtension(): GlobalInspectionContextExtension<*> {
        return ParadoxGlobalInspectionContext()
    }
    
    override fun createRefManagerExtension(refManager: RefManager): RefManagerExtension<*> {
        return ParadoxRefManager(refManager)
    }
    
    override fun createHTMLComposerExtension(composer: HTMLComposer): HTMLComposerExtension<*>? {
        return null
    }
    
    override fun isToCheckMember(element: PsiElement, id: String): Boolean {
        return SuppressManager.getInstance().getElementToolSuppressedIn(element, id) == null
    }
    
    override fun getSuppressedInspectionIdsIn(element: PsiElement): String? {
        return SuppressManager.getInstance().getSuppressedInspectionIdsIn(element)
    }
}