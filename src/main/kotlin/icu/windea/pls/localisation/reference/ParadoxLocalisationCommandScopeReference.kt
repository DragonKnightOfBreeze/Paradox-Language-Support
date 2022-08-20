package icu.windea.pls.localisation.reference

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxCommandScopeCompletionProvider
 */
class ParadoxLocalisationCommandScopeReference(
	element: ParadoxLocalisationCommandScope,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandScope>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		val name = element.name
		val project = element.project
		//处理字符串需要被识别为预先定义的localisation_command的情况
		return doResolveLocalisationScope(name, project)
	}
	
	private fun doResolveLocalisationScope(name: String, project: Project): PsiElement? {
		val gameType = element.fileInfo?.rootInfo?.gameType ?: return null
		val configGroup = getCwtConfig(project).get(gameType) ?: return null
		return CwtConfigHandler.resolveLocalisationScope(name, configGroup)
	}
}