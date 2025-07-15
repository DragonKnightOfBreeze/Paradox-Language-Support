package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.script.psi.*

/**
 * 提供对脚本表达式的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等功能。
 *
 * @see ParadoxScriptExpressionElement
 */
@WithGameTypeEP
interface ParadoxScriptExpressionSupport {
    fun supports(config: CwtConfig<*>): Boolean

    fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {

    }

    fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        return null
    }

    fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
        return resolve(element, rangeInElement, expressionText, config, isKey, false).singleton().setOrEmpty()
    }

    fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Array<out PsiReference>? {
        return null
    }

    fun complete(context: ProcessingContext, result: CompletionResultSet) {

    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxScriptExpressionSupport>("icu.windea.pls.scriptExpressionSupport")

        //这里需要尝试避免SOE

        fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
            val gameType = config.configGroup.gameType
            withRecursionGuard {
                EP_NAME.extensionList.forEach f@{ ep ->
                    if (!ep.supports(config)) return@f
                    if (!gameType.supportsByAnnotation(ep)) return@f
                    withRecursionCheck("${ep.javaClass.name}@annotate@${expressionText}") {
                        ep.annotate(element, rangeInElement, expressionText, holder, config)
                    }
                }
            }
        }

        fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
            val gameType = config.configGroup.gameType
            return withRecursionGuard {
                EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                    if (!ep.supports(config)) return@f null
                    if (!gameType.supportsByAnnotation(ep)) return@f null
                    val r = withRecursionCheck("${ep.javaClass.name}@resolve@${expressionText}") {
                        ep.resolve(element, rangeInElement, expressionText, config, isKey, exact)
                    }
                    r
                }
            }
        }

        fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
            val gameType = config.configGroup.gameType
            return withRecursionGuard {
                EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                    if (!ep.supports(config)) return@f null
                    if (!gameType.supportsByAnnotation(ep)) return@f null
                    val r = withRecursionCheck("${ep.javaClass.name}@multiResolve@${expressionText}") {
                        ep.multiResolve(element, rangeInElement, expressionText, config, isKey).orNull()
                    }
                    r
                }
            }.orEmpty()
        }

        fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Array<out PsiReference>? {
            val gameType = config.configGroup.gameType
            return withRecursionGuard {
                EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                    if (!ep.supports(config)) return@f null
                    if (!gameType.supportsByAnnotation(ep)) return@f null
                    val r = withRecursionCheck("${ep.javaClass.name}@multiResolve@${expressionText}") {
                        ep.getReferences(element, rangeInElement, expressionText, config, isKey).orNull()
                    }
                    r
                }
            }
        }

        fun complete(context: ProcessingContext, result: CompletionResultSet) {
            val config = context.config ?: return
            val gameType = config.configGroup.gameType
            withRecursionGuard {
                EP_NAME.extensionList.forEach f@{ ep ->
                    if (!ep.supports(config)) return@f
                    if (!gameType.supportsByAnnotation(ep)) return@f
                    withRecursionCheck("${ep.javaClass.name}@complete${context.keyword}") {
                        ep.complete(context, result)
                    }
                }
            }
        }
    }
}

