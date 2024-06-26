package icu.windea.pls.localisation.references

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.highlighter.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationCommandFieldCompletionProvider
 */
class ParadoxLocalisationCommandFieldPsiReference(
	element: ParadoxLocalisationCommandField,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement) {
	val project by lazy { element.project }
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(rangeInElement.replace(element.text, newElementName))
	}
	
	//缓存解析结果以优化性能
	
	private object Resolver: ResolveCache.AbstractResolver<ParadoxLocalisationCommandFieldPsiReference, PsiElement> {
		override fun resolve(ref: ParadoxLocalisationCommandFieldPsiReference, incompleteCode: Boolean): PsiElement? {
			return ref.doResolve()
		}
	}
	
	private object MultiResolver: ResolveCache.PolyVariantResolver<ParadoxLocalisationCommandFieldPsiReference> {
		override fun resolve(ref: ParadoxLocalisationCommandFieldPsiReference, incompleteCode: Boolean): Array<out ResolveResult> {
			return ref.doMultiResolve()
		}
	}
	
	override fun resolve(): PsiElement? {
		return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
	}
	
	private fun doResolve(): PsiElement? {
		val element = element
		val name = element.name
		val gameType = selectGameType(element) ?: return null
		val configGroup = getConfigGroup(project, gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = CwtConfigHandler.resolvePredefinedLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return localisationCommand
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector(project, element).contextSensitive()
		val scriptedLoc = ParadoxDefinitionSearch.search(name, "scripted_loc", selector).find()
		if(scriptedLoc != null) return scriptedLoc
		
		//尝试识别为预定义的value[variable] （忽略大小写）
		val predefinedVariable = configGroup.dynamicValueTypes.get("variable")?.valueConfigMap?.get(name)
		if(predefinedVariable != null) return predefinedVariable.pointer.element
		
		//尝试识别为value[variable]
		return ParadoxDynamicValueElement(element, name, "variable", Access.Read, gameType, project)
	}
	
	private fun doMultiResolve(): Array<out ResolveResult> {
		val element = element
		val name = element.name
		val gameType = selectGameType(element) ?: return ResolveResult.EMPTY_ARRAY
		val configGroup = getConfigGroup(project, gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = CwtConfigHandler.resolvePredefinedLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return arrayOf(PsiElementResolveResult(localisationCommand))
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector(project, element).contextSensitive()
		val scriptedLocs = ParadoxDefinitionSearch.search(name, "scripted_loc", selector).findAll()
		if(scriptedLocs.isNotEmpty()) return scriptedLocs.mapToArray { PsiElementResolveResult(it) }
		
		//尝试识别为预定义的value[variable] （忽略大小写）
		val predefinedVariable = configGroup.dynamicValueTypes.get("variable")?.valueConfigMap?.get(name)
		if(predefinedVariable != null) return predefinedVariable.pointer.element?.let { arrayOf(PsiElementResolveResult(it)) } ?: ResolveResult.EMPTY_ARRAY
		
		//尝试识别为value[variable]
		return arrayOf(PsiElementResolveResult(ParadoxDynamicValueElement(element, name, "variable", Access.Read, gameType, project)))
	}
	
	fun getAttributesKey(): TextAttributesKey? {
		val element = element
		val name = element.name
		val gameType = selectGameType(element) ?: return null
		val configGroup = getConfigGroup(project, gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = CwtConfigHandler.resolvePredefinedLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return null //no highlight
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector(project, element)
		val scriptedLoc = ParadoxDefinitionSearch.search(name, "scripted_loc", selector).findFirst()
		if(scriptedLoc != null) return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY //definition reference
		
		//尝试识别为预定义的value[variable] （忽略大小写）
		val predefinedVariable = configGroup.dynamicValueTypes.get("variable")?.valueConfigMap?.get(name)
		if(predefinedVariable != null) return ParadoxScriptAttributesKeys.VARIABLE_KEY
		
		//尝试识别为value[variable]
		return ParadoxScriptAttributesKeys.VARIABLE_KEY
	}
}
