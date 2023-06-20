package icu.windea.ut.markdown

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.registry.*
import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import org.intellij.plugins.markdown.lang.psi.impl.*

/**
 * 用于折叠标签文本为空的Markdown内联链接。（`[](...)`）
 */
@HiddenApi
@WithExtension("org.intellij.plugins.markdown")
@Suppress("UnstableApiUsage")
class MarkdownEmptyLinkFoldingBuilder: FoldingBuilderEx(), DumbAware {
    companion object {
        const val GROUP_NAME = "ut.markdown.emptyLink"
        val FOLDING_GROUP = FoldingGroup.newGroup(GROUP_NAME)
    }
    
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.accept(object : PsiRecursiveElementWalkingVisitor(){
            override fun visitElement(element: PsiElement) {
                if(element is MarkdownInlineLink) {
                    val linkText = element.linkText
                    if(linkText != null && linkText.contentElements.none()) {
                        descriptors.add(FoldingDescriptor(element.node, element.textRange, FOLDING_GROUP))
                    }
                }
                super.visitElement(element)
            }
        })
        return descriptors.toTypedArray()
    }
    
    override fun getPlaceholderText(node: ASTNode) = ""
    
    override fun isCollapsedByDefault(node: ASTNode) = Registry.`is`("ut.markdown.fold.empty.link.by.default")
}