package com.windea.plugin.idea.paradox.localisation.formatter

import com.intellij.formatting.*
import com.intellij.formatting.Indent
import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.formatter.common.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.*
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*

//调试没有问题就不要随便修改

class ParadoxLocalisationBlock(
	node: ASTNode,
	private val settings: CodeStyleSettings
) : AbstractBlock(node, createWrap(), createAlignment()) {
	companion object {
		private fun createWrap(): Wrap? {
			return null
		}

		private val rootAlignment = Alignment.createAlignment()
		private val alignment = Alignment.createAlignment()

		private fun createAlignment(): Alignment? {
			return null
		}

		private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
			//数字和属性值之间有一个空格，冒号和属性值之间也有
			return SpacingBuilder(settings, ParadoxLocalisationLanguage)
				.between(COLON, PROPERTY_VALUE).spaces(1)
				.between(NUMBER, PROPERTY_VALUE).spaces(1)
		}
	}

	private val spacingBuilder = createSpacingBuilder(settings)

	//收集所有节点
	override fun buildChildren(): List<Block> {
		return myNode.nodes().map { ParadoxLocalisationBlock(it, settings) }
	}

	override fun getIndent(): Indent? {
		//属性和非头部非行尾注释要缩进
		return when(myNode.elementType) {
			COMMENT, PROPERTY -> Indent.getNormalIndent()
			else -> Indent.getNoneIndent()
		}
	}

	override fun getChildIndent(): Indent? {
		//配置换行时的自动缩进
		//在psiFile中不要缩进
		return when {
			myNode.psi is PsiFile -> Indent.getNoneIndent()
			else -> null
		}
	}

	override fun getSpacing(child1: Block?, child2: Block): Spacing? {
		//顶级快没有spacing
		return spacingBuilder.getSpacing(this, child1, child2)
	}

	override fun isLeaf(): Boolean {
		//顶级块不是叶子节点
		return myNode.firstChildNode == null
	}
}
