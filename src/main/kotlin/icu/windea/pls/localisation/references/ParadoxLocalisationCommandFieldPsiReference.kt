package icu.windea.pls.localisation.references

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.*
import icu.windea.pls.core.*
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
) : PsiPolyVariantReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement), AttributesKeyAware {
	val project by lazy { element.project }
	
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	//缓存解析结果以优化性能
	
	override fun resolve(): PsiElement? {
		if(PlsThreadLocals.defaultResolveToValueSetValue.get()) {
			return doResolve()
		}
		return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
	}
	
	private fun doResolve(): PsiElement? {
		val element = element
		val name = element.name
		val gameType = selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).get(gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = ParadoxConfigHandler.resolvePredefinedLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return localisationCommand
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector(project, element).contextSensitive()
		val scriptedLoc = ParadoxDefinitionSearch.search(name, "scripted_loc", selector).find()
		if(scriptedLoc != null) return scriptedLoc
		
		//尝试识别为预定义的value[variable] （忽略大小写）
		val predefinedVariable = configGroup.values.get("variable")?.valueConfigMap?.get(name)
		if(predefinedVariable != null) return predefinedVariable.pointer.element
		
		//尝试识别为value[variable]（需要预先在脚本文件中使用到）
		if(PlsThreadLocals.defaultResolveToValueSetValue.get()) {
			return ParadoxValueSetValueElement(element, name, "variable", Access.Read, gameType, project)
		}
		val variableSelector = valueSetValueSelector(project, element).contextSensitive()
		val variable = ParadoxValueSetValueSearch.search(name, "variable", variableSelector).findFirst()
		if(variable != null) return ParadoxValueSetValueElement(element, name, "variable", Access.Read, gameType, project)
		
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		if(PlsThreadLocals.defaultResolveToValueSetValue.get()) {
			return doMultiResolve()
		}
		return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
	}
	
	private fun doMultiResolve(): Array<out ResolveResult> {
		val element = element
		val name = element.name
		val gameType = selectGameType(element) ?: return ResolveResult.EMPTY_ARRAY
		val configGroup = getCwtConfig(project).get(gameType)
		
		//尝试识别为预定义的localisation_command
		val localisationCommand = ParadoxConfigHandler.resolvePredefinedLocalisationCommand(name, configGroup)
		if(localisationCommand != null) return arrayOf(PsiElementResolveResult(localisationCommand))
		
		//尝试识别为<scripted_loc>
		val selector = definitionSelector(project, element).contextSensitive()
		val scriptedLocs = ParadoxDefinitionSearch.search(name, "scripted_loc", selector).findAll()
		if(scriptedLocs.isNotEmpty()) return scriptedLocs.mapToArray { PsiElementResolveResult(it) }
		
		//尝试识别为预定义的value[variable] （忽略大小写）
		val predefinedVariable = configGroup.values.get("variable")?.valueConfigMap?.get(name)
		if(predefinedVariable != null) return predefinedVariable.pointer.element?.let { arrayOf(PsiElementResolveResult(it)) } ?: ResolveResult.EMPTY_ARRAY
		
		//尝试识别为value[variable]（需要预先在脚本文件中使用到）
		if(PlsThreadLocals.defaultResolveToValueSetValue.get()) {
			return arrayOf(PsiElementResolveResult(ParadoxValueSetValueElement(element, name, "variable", Access.Read, gameType, project)))
		}
		val variableSelector = valueSetValueSelector(project, element).contextSensitive()
		val variable = ParadoxValueSetValueSearch.search(name, "variable", variableSelector).findFirst()
		if(variable != null) return arrayOf(PsiElementResolveResult(ParadoxValueSetValueElement(element, name, "variable", Access.Read, gameType, project)))
		
		return ResolveResult.EMPTY_ARRAY
	}
	
	override fun getAttributesKey(): TextAttributesKey? {
		val element = element
		val name = element.name
		val gameType = selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).get(gameType)
		
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
		
		//尝试识别为value[variable]（需要预先在脚本文件中使用到）
		if(PlsThreadLocals.defaultResolveToValueSetValue.get()) {
			return ParadoxScriptAttributesKeys.VARIABLE_KEY
		}
		val variableSelector = valueSetValueSelector(project, element)
		val variable = ParadoxValueSetValueSearch.search(name, "variable", variableSelector).findFirst()
		if(variable != null) return ParadoxScriptAttributesKeys.VARIABLE_KEY
		
		return null
	}
	
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
}
