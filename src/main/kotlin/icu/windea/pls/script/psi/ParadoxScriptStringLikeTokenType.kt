package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

@Deprecated("")
object ParadoxScriptStringLikeTokenType : ILazyParseableElementType(
	"STRING_LIKE_TOKEN",
	ParadoxScriptLanguage
) {
	override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
		val project = psi.project
		run {
			val definition = psi.findParentDefinition() ?: return@run
			val definitionInfo = definition.definitionInfo ?: return@run
			//如果对应游戏类型的CWT标签规则存在且不为空，且对应名称的标签匹配当前PsiElement所属定义的类型，则解析为TAG_TOKEN
			val tags = getCwtConfig(project).getValue(definitionInfo.gameType).tags
			if(tags.isEmpty()) return@run
			val tag = tags[chameleon.text] ?: return@run
			if(tag.supportedTypes.contains(definitionInfo.type)) return parseToTag(chameleon)
		}
		//解析为STRING_TOKEN
		return parseToString(chameleon)
	}
	
	private fun parseToTag(chameleon: ASTNode): ASTNode {
		return ASTFactory.leaf(TAG_TOKEN, chameleon.text)
		//val node = ASTFactory.composite(ParadoxScriptElementTypes.TAG)
		//node.rawAddChildrenWithoutNotifications(ASTFactory.leaf(ParadoxScriptElementTypes.TAG_TOKEN, chameleon.text))
		//return node
	}
	
	private fun parseToString(chameleon: ASTNode): ASTNode {
		return ASTFactory.leaf(STRING_TOKEN, chameleon.text)
		//val node = ASTFactory.composite(ParadoxScriptElementTypes.STRING)
		//node.rawAddChildrenWithoutNotifications(ASTFactory.leaf(ParadoxScriptElementTypes.STRING_TOKEN, chameleon.text))
		//return node
	}
}