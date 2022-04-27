package icu.windea.pls.script.formatter

import com.intellij.formatting.*
import com.intellij.formatting.Indent
import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.formatter.common.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptBlock(
	node: ASTNode,
	private val settings: CodeStyleSettings,
) : AbstractBlock(node, createWrap(), createAlignment()) {
	companion object {
		private fun createWrap(): Wrap? {
			return null
		}

		private fun createAlignment(): Alignment? {
			return null
		}

		private fun createSpacingBuilder(node:ASTNode,settings: CodeStyleSettings): SpacingBuilder {
			//变量声明分隔符周围的空格，属性分隔符周围的空格
			val customSettings = settings.getCustomSettings(ParadoxScriptCodeStyleSettings::class.java)
			val spaceWithinBraces = customSettings.SPACE_WITHIN_BRACES
			val spaceAroundSeparator = customSettings.SPACE_AROUND_SEPARATOR
			val endOfLine = node.treeNext?.let{ it.elementType == TokenType.WHITE_SPACE && it.text.containsLineBreak() }?: false
			return SpacingBuilder(settings, ParadoxScriptLanguage)
				.between(LEFT_BRACE, RIGHT_BRACE).spaces(0) //花括号之间总是不需要空格
				.after(LEFT_BRACE).spaceIf(!endOfLine && spaceWithinBraces) //左花括号之后如果非换行按情况可能需要空格
				.before(RIGHT_BRACE).spaceIf(!endOfLine && spaceWithinBraces) //右花括号之前如果非换行按情况可能需要空格
				.around(EQUAL_SIGN).spaceIf(spaceAroundSeparator) //等号周围按情况可能需要空格
		}
	}

	private val spacingBuilder = createSpacingBuilder(myNode,settings)

	override fun buildChildren(): List<Block> {
		return myNode.nodes().map { ParadoxScriptBlock(it, settings) }
	}

	override fun getIndent(): Indent? {
		//配置缩进
		//block中的属性、值、注释需要缩进
		val elementType = myNode.elementType
		val parentElementType = myNode.treeParent?.elementType
		return when {
			parentElementType != BLOCK -> Indent.getNoneIndent()
			elementType == LEFT_BRACE || elementType == RIGHT_BRACE -> Indent.getNoneIndent()
			else -> Indent.getNormalIndent()
		}
	}

	override fun getChildIndent(): Indent? {
		//配置换行时的自动缩进
		//在file和rootBlock中不要缩进，在block中要缩进
		val elementType = myNode.elementType
		return when{
			elementType is IFileElementType -> Indent.getNoneIndent()
			elementType == ROOT_BLOCK -> Indent.getNoneIndent()
			elementType == BLOCK -> Indent.getNormalIndent()
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
