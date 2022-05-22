package icu.windea.pls.localisation.reference

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxCommandFieldCompletionProvider
 */
class ParadoxLocalisationCommandFieldReference(
	element: ParadoxLocalisationCommandField,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		val name = element.name
		val project = element.project
		//处理字符串需要被识别为预先定义的localisation_command的情况
		doResolveLocalisationCommand(name, project)?.let { return it }
		//解析为类型为scripted_loc的definition
		return findDefinitionByType(name, "scripted_loc", project)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		val name = element.name
		val project = element.project
		//处理字符串需要被识别为预先定义的localisation_command的情况
		doResolveLocalisationCommand(name, project)?.let { return arrayOf(PsiElementResolveResult(it)) }
		//解析为类型为scripted_loc的definition
		return findDefinitionsByType(name, "scripted_loc", project).mapToArray { PsiElementResolveResult(it) }
	}
	
	private fun doResolveLocalisationCommand(name: String, project: Project): CwtProperty? {
		val gameType = element.fileInfo?.gameType ?: return null
		val configGroup = getCwtConfig(project).get(gameType) ?: return null
		return resolveLocalisationCommand(name, configGroup)
	}
	
	//代码提示功能由ParadoxCommandFieldCompletionProvider实现
}
