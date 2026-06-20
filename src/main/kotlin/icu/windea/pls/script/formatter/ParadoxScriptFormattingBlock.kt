package icu.windea.pls.script.formatter

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
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptFormattingBlock(
    node: ASTNode,
    private val settings: CodeStyleSettings,
) : AbstractBlock(node, createWrap(), createAlignment()) {
    companion object {
        private val MEMBERS = TokenSet.create(SCRIPTED_VARIABLE, PROPERTY, BOOLEAN, INT, FLOAT, STRING, COLOR, INLINE_MATH, PARAMETER, BLOCK, CONDITIONAL_BLOCK)
        private val NORMAL_SEPARATORS = TokenSet.create(EQUAL_SIGN, NOT_EQUAL_SIGN, LT_SIGN, GT_SIGN, LE_SIGN, GE_SIGN, SAFE_ASSIGN_SIGN)
        private val LEADING_SEPARATORS = TokenSet.create(SAFE_CALL_ASSIGN_SIGN)
        private val INLINE_MATH_OPERATORS = TokenSet.create(PLUS_SIGN, MINUS_SIGN, TIMES_SIGN, DIV_SIGN, MOD_SIGN, LABS_SIGN, RABS_SIGN, LP_SIGN, RP_SIGN)
        private val SHOULD_INDENT_PARENT_TYPES = TokenSet.create(BLOCK, CONDITIONAL_BLOCK)
        private val SHOULD_INDENT_TYPES = TokenSet.create(SCRIPTED_VARIABLE, PROPERTY, BOOLEAN, INT, FLOAT, STRING, COLOR, INLINE_MATH, PARAMETER, BLOCK, CONDITIONAL_BLOCK, COMMENT)
        private val SHOULD_CHILD_INDENT_TYPES = TokenSet.create(BLOCK, CONDITIONAL_BLOCK, CONDITIONAL_BLOCK_EXPRESSION)

        private fun createWrap(): Wrap? {
            return null
        }

        private fun createAlignment(): Alignment? {
            return null
        }

        private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
            // NOTE #123 - 目前对于格式化不需要进行额外的处理

            // 变量声明分隔符周围的空格，属性分隔符周围的空格
            val customSettings = settings.getCustomSettings(ParadoxScriptCodeStyleSettings::class.java)
            return SpacingBuilder(settings, ParadoxScriptLanguage)
                .between(MEMBERS, MEMBERS).spaces(1) // 封装变量/属性/值/参数化快之间需要有空格或者换行
                .aroundInside(NORMAL_SEPARATORS, SCRIPTED_VARIABLE).spaceIf(customSettings.SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR) // 间隔符周围按情况可能需要空格
                .beforeInside(LEADING_SEPARATORS, SCRIPTED_VARIABLE).spaces(0) // 间隔符周围按情况可能需要空格（强制移除左侧空白）
                .afterInside(LEADING_SEPARATORS, SCRIPTED_VARIABLE).spaceIf(customSettings.SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR) // 间隔符周围按情况可能需要空格（强制移除左侧空白）
                .aroundInside(NORMAL_SEPARATORS, PROPERTY).spaceIf(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) // 间隔符周围按情况可能需要空格
                .beforeInside(LEADING_SEPARATORS, PROPERTY).spaces(0) // 间隔符周围按情况可能需要空格（强制移除左侧空白）
                .afterInside(LEADING_SEPARATORS, PROPERTY).spaceIf(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) // 间隔符周围按情况可能需要空格（强制移除左侧空白）
                .around(INLINE_MATH_OPERATORS).spaceIf(customSettings.SPACE_AROUND_INLINE_MATH_OPERATOR) // 内联数学表达式运算符周围按情况可能需要空格
                .between(LEFT_BRACE, RIGHT_BRACE).spaceIf(customSettings.SPACE_WITHIN_EMPTY_BRACES) // 花括号之间按情况可能需要空格
                .withinPair(LEFT_BRACE, RIGHT_BRACE).spaceIf(customSettings.SPACE_WITHIN_BRACES, true) // 花括号内侧按情况可能需要空格
                .between(NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET).none() // 参数化快表达式如果为空则不需要空格（尽管这是语法错误）
                .withinPair(NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET).spaceIf(customSettings.SPACE_WITHIN_CONDITIONAL_BLOCK_EXPRESSION_BRACKETS) // 参数化快表达式内侧非换行按情况可能需要空格
                .between(NESTED_RIGHT_BRACKET, RIGHT_BRACKET).none() // 参数化快代码块如果为空则不需要空格
                .between(NESTED_RIGHT_BRACKET, MEMBERS).spaceIf(customSettings.SPACE_WITHIN_CONDITIONAL_BLOCK_BRACKETS, true)
                .between(MEMBERS, RIGHT_BRACKET).spaceIf(customSettings.SPACE_WITHIN_CONDITIONAL_BLOCK_BRACKETS, true)
                .between(INLINE_MATH_START, INLINE_MATH_END).none() // 内联数字表达式如果为空则不需要空格（尽管这是语法错误）
                .withinPair(INLINE_MATH_START, INLINE_MATH_END).spaceIf(customSettings.SPACE_WITHIN_INLINE_MATH_BRACKETS, true) // 内联数学表达式内侧按情况可能需要空格
        }
    }

    private val spacingBuilder = createSpacingBuilder(settings)

    override fun buildChildren(): List<Block> {
        val children = mutableListOf<Block>()
        myNode.processChild p@{ node ->
            if (node.elementType == TokenType.WHITE_SPACE) return@p true
            children += ParadoxScriptFormattingBlock(node, settings)
            true
        }
        return children
    }

    override fun getIndent(): Indent? {
        // 配置缩进
        // `block` 和 `conditional_block` 中的 `variable` `property` `value` `conditional_block` 和 `comment` 需要缩进
        val elementType = myNode.elementType
        val parentElementType = myNode.treeParent?.elementType
        return when {
            parentElementType in SHOULD_INDENT_PARENT_TYPES && elementType in SHOULD_INDENT_TYPES -> Indent.getNormalIndent()
            else -> Indent.getNoneIndent()
        }
    }

    override fun getChildIndent(): Indent? {
        // 配置换行时的自动缩进
        // 在 `file` 和 `rootBlock` 中不要缩进
        // 在 `block` `conditional_block` `conditional_block_expression` 中需要缩进
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
        // 顶级块不是叶子节点
        return myNode.firstChildNode == null
    }
}
