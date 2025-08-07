package icu.windea.pls.ep.parameter

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.parameter.ParadoxParameterSupport.Keys.synced
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.model.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.script.psi.*

/**
 * 提供对脚本参数的支持。
 *
 * @see icu.windea.pls.lang.psi.mock.ParadoxParameterElement
 */
interface ParadoxParameterSupport {
    fun isContext(element: ParadoxScriptDefinitionElement): Boolean

    fun findContext(element: PsiElement): ParadoxScriptDefinitionElement?

    fun getContextKeyFromContext(context: ParadoxScriptDefinitionElement): String?

    fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo?

    /**
     * 向上查找参数的上下文引用信息。
     *
     * @param element 开始查找的位置。
     * @param from 从哪里向上查找。
     * @param extraArgs 对于每个实现需要的额外参数可能是不同的。
     * @see icu.windea.pls.lang.codeInsight.parameterInfo.ParadoxParameterInfoHandler
     */
    fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo?

    fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement?

    fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement?

    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement?

    /**
     * @param onlyMostRelevant 是否只遍历最相关的那个上下文。
     * @return 此扩展点是否适用。
     */
    fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean

    /**
     * @param onlyMostRelevant 是否只遍历最相关的那个上下文。
     * @return 此扩展点是否适用。
     */
    fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean

    fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker? = null

    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxParameterSupport>("icu.windea.pls.parameterSupport")

        fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
            return EP_NAME.extensionList.any { it.isContext(element) }
        }

        fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.findContext(element) }
        }

        fun getContextKeyFromContext(element: ParadoxScriptDefinitionElement): String? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.getContextKeyFromContext(element) }
        }

        fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getContextInfo(element)
            }
        }

        fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getContextReferenceInfo(element, from, *extraArgs)
                    ?.also { it.support = ep }
            }
        }

        fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveParameter(element)
                    ?.also { it.support = ep }
            }
        }

        fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveConditionParameter(element)
                    ?.also { it.support = ep }
            }
        }

        fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveArgument(element, rangeInElement, config)
                    ?.also { it.support = ep }
            }
        }

        fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.processContext(parameterElement, onlyMostRelevant, processor)
            }
        }

        fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.processContext(element, contextReferenceInfo, onlyMostRelevant, processor)
            }
        }

        fun getDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.buildDocumentationDefinition(parameterElement, builder)
            }
        }
    }

    object Keys : KeyRegistry() {
        val keysToSync: MutableSet<Key<*>> = mutableSetOf()

        //use optimized method rather than UserDataHolderBase.copyUserDataTo to reduce memory usage
        fun syncUserData(from: UserDataHolder, to: UserDataHolder) {
            keysToSync.forEach { key ->
                @Suppress("UNCHECKED_CAST")
                key as Key<Any>
                to.putUserData(key, from.getUserData(key))
            }
        }

        fun <T : KeyProvider<*>> T.synced() = apply { callback { keysToSync += it } }
    }
}

val ParadoxParameterSupport.Keys.support by createKey<ParadoxParameterSupport>(ParadoxParameterSupport.Keys).synced()
val ParadoxParameterSupport.Keys.containingContext by createKey<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>(ParadoxParameterSupport.Keys).synced()
val ParadoxParameterSupport.Keys.containingContextReference by createKey<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>(ParadoxParameterSupport.Keys).synced()
val ParadoxParameterSupport.Keys.definitionName by createKey<String>(ParadoxParameterSupport.Keys).synced()
val ParadoxParameterSupport.Keys.definitionTypes by createKey<List<String>>(ParadoxParameterSupport.Keys).synced()
val ParadoxParameterSupport.Keys.inlineScriptExpression by createKey<String>(ParadoxParameterSupport.Keys).synced()

var ParadoxParameterInfo.support by ParadoxParameterSupport.Keys.support
var ParadoxParameterInfo.definitionName by ParadoxParameterSupport.Keys.definitionName
var ParadoxParameterInfo.definitionTypes by ParadoxParameterSupport.Keys.definitionTypes
var ParadoxParameterInfo.inlineScriptExpression by ParadoxParameterSupport.Keys.inlineScriptExpression

var ParadoxParameterElement.support by ParadoxParameterSupport.Keys.support
var ParadoxParameterElement.containingContext by ParadoxParameterSupport.Keys.containingContext
var ParadoxParameterElement.containingContextReference by ParadoxParameterSupport.Keys.containingContextReference
var ParadoxParameterElement.definitionName by ParadoxParameterSupport.Keys.definitionName
var ParadoxParameterElement.definitionTypes by ParadoxParameterSupport.Keys.definitionTypes
var ParadoxParameterElement.inlineScriptExpression by ParadoxParameterSupport.Keys.inlineScriptExpression

var ParadoxParameterContextReferenceInfo.support by ParadoxParameterSupport.Keys.support
var ParadoxParameterContextReferenceInfo.containingContext by ParadoxParameterSupport.Keys.containingContext
var ParadoxParameterContextReferenceInfo.containingContextReference by ParadoxParameterSupport.Keys.containingContextReference
var ParadoxParameterContextReferenceInfo.definitionName by ParadoxParameterSupport.Keys.definitionName
var ParadoxParameterContextReferenceInfo.definitionTypes by ParadoxParameterSupport.Keys.definitionTypes
var ParadoxParameterContextReferenceInfo.inlineScriptExpression by ParadoxParameterSupport.Keys.inlineScriptExpression
