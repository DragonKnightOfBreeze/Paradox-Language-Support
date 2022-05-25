package icu.windea.pls.cwt.formatter

import com.intellij.formatting.*
import com.intellij.formatting.Indent
import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.formatter.common.*
import com.intellij.psi.tree.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.codeStyle.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtBlock(
	node: ASTNode,
	private val settings: CodeStyleSettings
) : AbstractBlock(node, createWrap(), createAlignment()) {
	companion object {
		private val separatorTokens = TokenSet.create(EQUAL_SIGN, NOT_EQUAL_SIGN)
		private val shouldIndentParentTypes = TokenSet.create(BLOCK)
		private val shouldIndentTypes = TokenSet.create(PROPERTY, VALUE, BOOLEAN, INT, FLOAT, STRING, COMMENT, DOCUMENTATION_COMMENT, OPTION_COMMENT)
		private val shouldChildIndentTypes = TokenSet.create(BLOCK)
		
		private fun createWrap(): Wrap? {
			return null
		}
		
		private fun createAlignment(): Alignment? {
			return null
		}
		
		private fun createSpacingBuilder(node: ASTNode, settings: CodeStyleSettings): SpacingBuilder {
			//变量声明分隔符周围的空格，属性分隔符周围的空格
			val customSettings = settings.getCustomSettings(CwtCodeStyleSettings::class.java)
			val endOfLine = node.treeNext?.let { it.elementType == TokenType.WHITE_SPACE && it.text.containsLineBreak() } ?: false
			return SpacingBuilder(settings, CwtLanguage)
				.aroundInside(separatorTokens, OPTION).spaceIf(customSettings.SPACE_AROUND_OPTION_SEPARATOR) //等号、不等号周围按情况可能需要空格
				.aroundInside(separatorTokens, PROPERTY).spaceIf(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) //等号、不等号周围按情况可能需要空格
				.between(LEFT_BRACE, RIGHT_BRACE).spaces(0) //花括号之间总是不需要空格
				.after(LEFT_BRACE).spaceIf(!endOfLine && customSettings.SPACE_WITHIN_BRACES) //左花括号之后如果非换行按情况可能需要空格
				.before(RIGHT_BRACE).spaceIf(!endOfLine && customSettings.SPACE_WITHIN_BRACES) //右花括号之前如果非换行按情况可能需要空格
		}
	}
	
	private val spacingBuilder = createSpacingBuilder(myNode, settings)
	
	override fun buildChildren(): List<Block> {
		val children = SmartList<Block>()
		myNode.processChildren { node -> node.takeUnless(TokenType.WHITE_SPACE)?.let { CwtBlock(it, settings) }?.addTo(children).end() }
		return children
	}
	
	override fun getIndent(): Indent? {
		//配置缩进
		//block中的property、value、comment、documentation_comment和option_comment需要缩进
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
		//在block中需要缩进
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
		return myNode.firstChildNode == null
	}
}

