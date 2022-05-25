package icu.windea.pls.script.formatter

import com.intellij.formatting.*
import com.intellij.formatting.Indent
import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.formatter.common.*
import com.intellij.psi.tree.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptBlock(
	node: ASTNode,
	private val settings: CodeStyleSettings,
) : AbstractBlock(node, createWrap(), createAlignment()) {
	companion object {
		private val separatorTokens = TokenSet.create(EQUAL_SIGN, NOT_EQUAL_SIGN, LT_SIGN, GT_SIGN, LE_SIGN, GE_SIGN)
		private val inlineMathOperatorTokens = TokenSet.create(PLUS_SIGN, MINUS_SIGN, TIMES_SIGN, DIV_SIGN, MOD_SIGN, LABS_SIGN, RABS_SIGN, LP_SIGN, RP_SIGN)
		private val shouldIndentParentTypes = TokenSet.create(BLOCK, PARAMETER_CONDITION)
		private val shouldIndentTypes = TokenSet.create(VARIABLE, PROPERTY, VALUE, BOOLEAN, INT, FLOAT, STRING, COLOR, INLINE_MATH, PARAMETER, STRING_TEMPLATE, PARAMETER_CONDITION, COMMENT)
		private val shouldChildIndentTypes = TokenSet.create(BLOCK, PARAMETER_CONDITION, PARAMETER_CONDITION_EXPRESSION)
		
		private fun createWrap(): Wrap? {
			return null
		}
		
		private fun createAlignment(): Alignment? {
			return null
		}
		
		private fun createSpacingBuilder(node: ASTNode, settings: CodeStyleSettings): SpacingBuilder {
			//变量声明分隔符周围的空格，属性分隔符周围的空格
			val customSettings = settings.getCustomSettings(ParadoxScriptCodeStyleSettings::class.java)
			val endOfLine = node.treeNext?.let { it.elementType == TokenType.WHITE_SPACE && it.text.containsLineBreak() } ?: false
			return SpacingBuilder(settings, ParadoxScriptLanguage)
				.aroundInside(separatorTokens, VARIABLE).spaceIf(customSettings.SPACE_AROUND_VARIABLE_SEPARATOR) //间隔符周围按情况可能需要空格
				.aroundInside(separatorTokens, PROPERTY).spaceIf(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) //间隔符周围按情况可能需要空格
				.around(inlineMathOperatorTokens).spaceIf(customSettings.SPACE_AROUND_INLINE_MATH_OPERATOR) //内联数学表达式操作符周围按情况可能需要空格
				.after(INLINE_MATH_START).spaceIf(!endOfLine && customSettings.SPACE_WITHIN_INLINE_MATH_BRACKETS) //内联数学表达式开始后如果非换行按情况可能需要空格
				.before(INLINE_MATH_END).spaceIf(!endOfLine && customSettings.SPACE_WITHIN_INLINE_MATH_BRACKETS) //内联数学表达式结束前如果非换行按情况可能需要空格
				.between(LEFT_BRACE, RIGHT_BRACE).spaces(0) //花括号之间总是不需要空格
				.after(LEFT_BRACE).spaceIf(!endOfLine && customSettings.SPACE_WITHIN_BRACES) //左花括号之后如果非换行按情况可能需要空格
				.before(RIGHT_BRACE).spaceIf(!endOfLine && customSettings.SPACE_WITHIN_BRACES) //右花括号之前如果非换行按情况可能需要空格
		}
	}
	
	private val spacingBuilder = createSpacingBuilder(myNode, settings)
	
	override fun buildChildren(): List<Block> {
		val children = SmartList<Block>()
		myNode.processChildren { node -> node.takeUnless(TokenType.WHITE_SPACE)?.let { ParadoxScriptBlock(it, settings) }?.addTo(children).end() }
		return children
	}
	
	override fun getIndent(): Indent? {
		//配置缩进
		//block和parameter_condition中的variable、property、value、parameter_condition和comment需要缩进
		val elementType = myNode.elementType
		val parentElementType = myNode.treeParent?.elementType
		return when {
			parentElementType in shouldIndentParentTypes && elementType in shouldIndentTypes -> Indent.getNormalIndent()
			else -> Indent.getNoneIndent()
		}
	}
	
	override fun getChildIndent(): Indent? {
		//配置换行时的自动缩进
		//在file和rootBlock中不要缩进
		//在block、parameter_condition、parameter_condition_expression中需要缩进
		val elementType = myNode.elementType
		return when {
			elementType is IFileElementType -> Indent.getNoneIndent()
			elementType == ROOT_BLOCK -> Indent.getNoneIndent()
			elementType in shouldChildIndentTypes -> Indent.getNormalIndent()
			else -> null
		}
	}
	
	override fun getSpacing(child1: Block?, child2: Block): Spacing? {
		return spacingBuilder.getSpacing(this, child1, child2)
	}
	
	override fun isLeaf(): Boolean {
		//顶级块不是叶子节点
		return myNode.firstChildNode == null
	}
}
