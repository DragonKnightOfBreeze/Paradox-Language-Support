package icu.windea.pls.core.expression.nodes

import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueArgumentValueExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val scriptValueNode: ParadoxScriptValueExpressionNode?,
	val argumentNode: ParadoxScriptValueArgumentExpressionNode?,
	val configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
	override fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? {
		if(!getSettings().inference.argumentValueConfig) return null
		val parameterElement = argumentNode?.getReference(element)?.resolve() ?: return null
		return ParadoxParameterHandler.inferEntireConfig(parameterElement)
	}
	
	override fun getAttributesKey(): TextAttributesKey {
		val type = ParadoxDataType.resolve(text)
		return when {
			type.isBooleanType() -> ParadoxScriptAttributesKeys.KEYWORD_KEY
			type.isFloatType() -> ParadoxScriptAttributesKeys.NUMBER_KEY
			else -> ParadoxScriptAttributesKeys.STRING_KEY
		}
	}
	
	override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
		if(!getSettings().inference.argumentValueConfig) return null
		if(scriptValueNode == null) return null
		if(text.isEmpty()) return null
		val reference = scriptValueNode.getReference(element)
		if(reference?.resolve() == null) return null //skip if script value cannot be resolved
		if(argumentNode == null) return null
		val project = configGroup.project
		return Reference(element, rangeInExpression, project, null) { argumentNode.getReference(element)?.resolve() }
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueExpressionNode?, parameterNode: ParadoxScriptValueArgumentExpressionNode?, configGroup: CwtConfigGroup): ParadoxScriptValueArgumentValueExpressionNode {
			return ParadoxScriptValueArgumentValueExpressionNode(text, textRange, scriptValueNode, parameterNode, configGroup)
		}
	}
	
	class Reference(
		element: ParadoxScriptStringExpressionElement,
		rangeInElement: TextRange,
		val project: Project,
		val isKey: Boolean?,
		private val parameterElementResolver: () -> ParadoxParameterElement?
	): PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): PsiElement {
			val element = element
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement? {
			if(!getSettings().inference.argumentValueConfig) return null
			return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
		}
		
		private fun doResolve(): PsiElement? {
			//根据对应的expression进行解析
			val parameterElement = parameterElementResolver() ?: return null
			val config = ParadoxParameterHandler.inferEntireConfig(parameterElement) ?: return null
			return ParadoxConfigHandler.resolveScriptExpression(element, rangeInElement, config, config.expression, config.info.configGroup, isKey)
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
			if(!getSettings().inference.argumentValueConfig) return ResolveResult.EMPTY_ARRAY
			return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
		}
		
		private fun doMultiResolve(): Array<out ResolveResult> {
			//根据对应的expression进行解析
			val parameterElement = parameterElementResolver() ?: return ResolveResult.EMPTY_ARRAY
			val config = ParadoxParameterHandler.inferEntireConfig(parameterElement) ?: return ResolveResult.EMPTY_ARRAY
			return ParadoxConfigHandler.multiResolveScriptExpression(element, rangeInElement, config, config.expression, config.info.configGroup, false)
				.mapToArray { PsiElementResolveResult(it) }
		}
		
		private object Resolver: ResolveCache.AbstractResolver<Reference, PsiElement> {
			override fun resolve(ref: Reference, incompleteCode: Boolean): PsiElement? {
				return ref.doResolve()
			}
		}
		
		private object MultiResolver: ResolveCache.PolyVariantResolver<Reference> {
			override fun resolve(ref: Reference, incompleteCode: Boolean): Array<out ResolveResult> {
				return ref.doMultiResolve()
			}
		}
	}
}
