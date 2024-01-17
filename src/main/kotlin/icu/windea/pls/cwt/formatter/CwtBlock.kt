package icu.windea.pls.cwt.formatter

import com.intellij.formatting.*
import com.intellij.formatting.Indent
import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.formatter.common.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.codeStyle.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtBlock(
    node: ASTNode,
    private val settings: CodeStyleSettings
) : AbstractBlock(node, createWrap(), createAlignment()) {
    companion object {
        private val MEMBERS = TokenSet.create(PROPERTY, VALUE, BOOLEAN, INT, FLOAT, STRING, BLOCK)
        private val SEPARATORS = TokenSet.create(EQUAL_SIGN, NOT_EQUAL_SIGN)
        private val SHOULD_INDENT_PARENT_TYPES = TokenSet.create(BLOCK)
        private val SHOULD_INDENT_TYPES = TokenSet.create(PROPERTY, VALUE, BOOLEAN, INT, FLOAT, STRING, BLOCK, COMMENT, DOCUMENTATION_COMMENT, OPTION_COMMENT)
        private val SHOULD_CHILD_INDENT_TYPES = TokenSet.create(BLOCK)
        
        private fun createWrap(): Wrap? {
            return null
        }
        
        private fun createAlignment(): Alignment? {
            return null
        }
        
        private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
            //变量声明分隔符周围的空格，属性分隔符周围的空格
            val customSettings = settings.getCustomSettings(CwtCodeStyleSettings::class.java)
            return SpacingBuilder(settings, CwtLanguage)
                .between(MEMBERS, MEMBERS).spaces(1) //属性/值之间需要有空格或者换行
                .aroundInside(SEPARATORS, OPTION).spaceIf(customSettings.SPACE_AROUND_OPTION_SEPARATOR) //间隔符周围按情况可能需要空格
                .aroundInside(SEPARATORS, PROPERTY).spaceIf(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) //间隔符周围按情况可能需要空格
                .between(LEFT_BRACE, MEMBERS).spaceIf(customSettings.SPACE_WITHIN_BRACES, true)
                .between(MEMBERS, RIGHT_BRACE).spaceIf(customSettings.SPACE_WITHIN_BRACES, true)
                .withinPair(LEFT_BRACE, RIGHT_BRACE).spaceIf(customSettings.SPACE_WITHIN_BRACES, true) //花括号内侧按情况可能需要空格
                .between(LEFT_BRACE, RIGHT_BRACE).none() //花括号之间总是不需要空格
        }
    }
    
    private val spacingBuilder = createSpacingBuilder(settings)
    
    override fun buildChildren(): List<Block> {
        val children = mutableListOf<Block>()
        myNode.processChild { node ->
            node.takeUnless(TokenType.WHITE_SPACE)?.let { CwtBlock(it, settings) }?.also { children.add(it) }
            true
        }
        return children
    }
    
    override fun getIndent(): Indent? {
        //配置缩进
        //block中的property、value、comment、documentation_comment和option_comment需要缩进
        val elementType = myNode.elementType
        val parentElementType = myNode.treeParent?.elementType
        return when {
            parentElementType in SHOULD_INDENT_PARENT_TYPES && elementType in SHOULD_INDENT_TYPES -> Indent.getNormalIndent()
            else -> Indent.getNoneIndent()
        }
    }
    
    override fun getChildIndent(): Indent? {
        //配置换行时的自动缩进
        //在file和rootBlock中不要缩进
        //在block中需要缩进
        val elementType = myNode.elementType
        return when {
            elementType is IFileElementType -> Indent.getNoneIndent()
            elementType == ROOT_BLOCK -> Indent.getNoneIndent()
            elementType in SHOULD_CHILD_INDENT_TYPES -> Indent.getNormalIndent()
            else -> null
        }
    }
    
    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }
    
    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }
}

