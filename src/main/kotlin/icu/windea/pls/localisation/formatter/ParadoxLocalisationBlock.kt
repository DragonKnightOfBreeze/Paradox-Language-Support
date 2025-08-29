package icu.windea.pls.localisation.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.core.processChild
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLON
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMENT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_LIST
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_NUMBER
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_VALUE

class ParadoxLocalisationBlock(
    node: ASTNode,
    private val settings: CodeStyleSettings
) : AbstractBlock(node, createWrap(), createAlignment()) {
    companion object {
        private val SHOULD_INDENT_PARENT_TYPES = TokenSet.create(PROPERTY_LIST)
        private val SHOULD_INDENT_TYPES = TokenSet.create(PROPERTY, COMMENT)
        private val SHOULD_CHILD_INDENT_TYPES = TokenSet.create(PROPERTY_LIST)

        private fun createWrap(): Wrap? {
            return null
        }

        private fun createAlignment(): Alignment? {
            return null
        }

        private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
            //属性数字和属性值之间有一个空格，冒号和属性值之间也有
            return SpacingBuilder(settings, ParadoxLocalisationLanguage)
                .between(COLON, PROPERTY_VALUE).spaces(1)
                .between(PROPERTY_NUMBER, PROPERTY_VALUE).spaces(1)
        }
    }

    private val spacingBuilder = createSpacingBuilder(settings)

    override fun buildChildren(): List<Block> {
        val children = mutableListOf<Block>()
        myNode.processChild p@{ node ->
            if (node.elementType == TokenType.WHITE_SPACE) return@p true
            children += ParadoxLocalisationBlock(node, settings)
            true
        }
        return children
    }

    override fun getIndent(): Indent? {
        //配置缩进
        //property_list中的property和comment需要缩进
        //属性和非头部非行尾注释要缩进
        val elementType = myNode.elementType
        val parentElementType = myNode.treeParent?.elementType
        return when {
            parentElementType in SHOULD_INDENT_PARENT_TYPES && elementType in SHOULD_INDENT_TYPES -> Indent.getNormalIndent()
            else -> Indent.getNoneIndent()
        }
    }

    override fun getChildIndent(): Indent? {
        //配置换行时的自动缩进
        //在file中不要缩进
        //在property_list中需要缩进
        val elementType = myNode.elementType
        return when {
            elementType is IFileElementType -> Indent.getNoneIndent()
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
