package icu.windea.pls.localisation.formatter

import com.intellij.formatting.*
import com.intellij.formatting.Indent
import com.intellij.lang.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.formatter.common.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

//调试没有问题就不要随便修改

class ParadoxLocalisationBlock(
	node: ASTNode,
	private val settings: CodeStyleSettings,
	private val root: Boolean
) : AbstractBlock(node, createWrap(), createAlignment()) {
	companion object {
		private fun createWrap(): Wrap? {
			return null
		}
		
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
		return myNode.nodes().map { ParadoxLocalisationBlock(it, settings, false) }
	}
	
	override fun getIndent(): Indent? {
		//配置缩进
		//属性和非头部非行尾注释要缩进
		val elementType = myNode.elementType
		return when {
			elementType == COMMENT -> if(root) Indent.getNoneIndent()  else Indent.getNormalIndent()
			elementType == PROPERTY -> Indent.getNormalIndent()
			else -> Indent.getNoneIndent()
		}
	}
	
	override fun getChildIndent(): Indent? {
		//配置换行时的自动缩进
		//在psiFile中不要缩进，在property_list中要缩进
		val elementType = myNode.elementType
		return when {
			elementType is IFileElementType -> Indent.getNoneIndent()
			elementType == PROPERTY_LIST -> Indent.getNormalIndent()
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
