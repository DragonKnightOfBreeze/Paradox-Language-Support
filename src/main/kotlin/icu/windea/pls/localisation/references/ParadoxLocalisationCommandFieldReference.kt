package icu.windea.pls.localisation.references

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.cwt.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationCommandFieldCompletionProvider
 */
class ParadoxLocalisationCommandFieldReference(
	element: ParadoxLocalisationCommandField,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement), PsiSmartReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的definition、valueSetValue
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			resolved is PsiNamedElement -> resolved.setName(newElementName)
			resolved is ParadoxScriptExpressionElement -> resolved.value = newElementName
			else -> throw IncorrectOperationException() //不允许重命名
		}
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): PsiElement? {
		val name = element.name
		val project = element.project
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).getValue(gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = CwtConfigHandler.resolveLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return localisationCommand
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
		val scriptedLoc = ParadoxDefinitionSearch.search(name, "scripted_loc", project, selector = selector).find()
		if(scriptedLoc != null) return scriptedLoc
		
		//尝试识别为value[variable]
		val variableSelector = valueSetValueSelector().gameType(gameType).preferRootFrom(element, exact)
		val variable = ParadoxValueSetValueSearch.search(name, "variable", project, selector = variableSelector).findFirst()
		if(variable != null) return ParadoxValueSetValueElement(element, name, "variable", project, gameType)
		
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		val name = element.name
		val project = element.project
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return ResolveResult.EMPTY_ARRAY
		val configGroup = getCwtConfig(project).getValue(gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = CwtConfigHandler.resolveLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return localisationCommand.let { arrayOf(PsiElementResolveResult(it)) }
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
		val scriptedLocs = ParadoxDefinitionSearch.search(name, "scripted_loc", project, selector = selector).findAll()
		if(scriptedLocs.isNotEmpty()) return scriptedLocs.mapToArray { PsiElementResolveResult(it) }
		
		//尝试识别为value[variable]
		val variableSelector = valueSetValueSelector().gameType(gameType).preferRootFrom(element)
		val variables = ParadoxValueSetValueSearch.search(name, "variable", project, selector = variableSelector).findAll()
		if(variables.isNotEmpty()) return variables.mapToArray { PsiElementResolveResult(ParadoxValueSetValueElement(element, name, "variable", project, gameType)) }
		
		return ResolveResult.EMPTY_ARRAY
	}
	
	override fun resolveTextAttributesKey(): TextAttributesKey? {
		val name = element.name
		val project = element.project
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).getValue(gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = CwtConfigHandler.resolveLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return null //no highlight
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector().gameType(gameType)
		val scriptedLoc = ParadoxDefinitionSearch.search(name, "scripted_loc", project, selector = selector).findFirst()
		if(scriptedLoc != null) return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY //definition reference
		
		//尝试识别为value[variable]
		val variableSelector = valueSetValueSelector().gameType(gameType)
		val variable = ParadoxValueSetValueSearch.search(name, "variable", project, selector = variableSelector).findFirst()
		if(variable != null) return ParadoxScriptAttributesKeys.VARIABLE_KEY
		
		return null
	}
}
