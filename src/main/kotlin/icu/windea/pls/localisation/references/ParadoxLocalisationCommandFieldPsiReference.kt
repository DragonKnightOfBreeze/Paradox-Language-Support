package icu.windea.pls.localisation.references

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.highlighter.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationCommandFieldCompletionProvider
 */
class ParadoxLocalisationCommandFieldPsiReference(
	element: ParadoxLocalisationCommandField,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement), PsiNodeReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): PsiElement? {
		val element = element
		val name = element.name
		val project = element.project
		val gameType = selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).getValue(gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = ParadoxConfigHandler.resolvePredefinedLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return localisationCommand
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector(project, element).contextSensitive(exact)
		val scriptedLoc = ParadoxDefinitionSearch.search(name, "scripted_loc", selector).find(exact)
		if(scriptedLoc != null) return scriptedLoc
		
		//尝试识别为预定义的value[variable] （忽略大小写）
		val predefinedVariable = configGroup.values.get("variable")?.valueConfigMap?.get(name)
		if(predefinedVariable != null) return predefinedVariable.pointer.element
		
		//尝试识别为value[variable]
		val variableSelector = valueSetValueSelector(project, element).contextSensitive(exact)
		val variable = ParadoxValueSetValueSearch.search(name, "variable", variableSelector).findFirst()
		if(variable != null) return ParadoxValueSetValueElement(element, name, "variable", Access.Read, gameType, project)
		
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		val element = element
		val name = element.name
		val project = element.project
		val gameType = selectGameType(element) ?: return ResolveResult.EMPTY_ARRAY
		val configGroup = getCwtConfig(project).getValue(gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = ParadoxConfigHandler.resolvePredefinedLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return localisationCommand.let { arrayOf(PsiElementResolveResult(it)) }
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector(project, element).contextSensitive()
		val scriptedLocs = ParadoxDefinitionSearch.search(name, "scripted_loc", selector).findAll()
		if(scriptedLocs.isNotEmpty()) return scriptedLocs.mapToArray { PsiElementResolveResult(it) }
		
		//尝试识别为预定义的value[variable] （忽略大小写）
		val predefinedVariable = configGroup.values.get("variable")?.valueConfigMap?.get(name)
		if(predefinedVariable != null) return predefinedVariable.pointer.element?.let { arrayOf(PsiElementResolveResult(it)) } ?: ResolveResult.EMPTY_ARRAY
		
		//尝试识别为value[variable]
		val variableSelector = valueSetValueSelector(project, element).contextSensitive()
		val variables = ParadoxValueSetValueSearch.search(name, "variable", variableSelector).findAll()
		if(variables.isNotEmpty()) return variables.mapToArray { PsiElementResolveResult(ParadoxValueSetValueElement(element, name, "variable", Access.Read, gameType, project)) }
		
		return ResolveResult.EMPTY_ARRAY
	}
	
	override fun getTextAttributesKey(): TextAttributesKey? {
		val element = element
		val name = element.name
		val project = element.project
		val gameType = selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).getValue(gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = ParadoxConfigHandler.resolvePredefinedLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return null //no highlight
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector(project, element)
		val scriptedLoc = ParadoxDefinitionSearch.search(name, "scripted_loc", selector).findFirst()
		if(scriptedLoc != null) return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY //definition reference
		
		//尝试识别为预定义的value[variable] （忽略大小写）
		val predefinedVariable = configGroup.values.get("variable")?.valueConfigMap?.get(name)
		if(predefinedVariable != null) return ParadoxScriptAttributesKeys.VARIABLE_KEY
		
		//尝试识别为value[variable]
		val variableSelector = valueSetValueSelector(project, element)
		val variable = ParadoxValueSetValueSearch.search(name, "variable", variableSelector).findFirst()
		if(variable != null) return ParadoxScriptAttributesKeys.VARIABLE_KEY
		
		return null
	}
}
