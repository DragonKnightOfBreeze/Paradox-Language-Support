package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.lang.codeInsight.ParadoxTypeManager
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.scope.toScopeIdMap
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 将脚本文本渲染为带注解的文本的渲染器。
 *
 * 说明：
 * - 可以配置详细的注解级别。参见 [ParadoxRendererAnnotatedLevel]。
 *
 * 支持的注解：
 * - 类型信息：`## type = x`, `## type = { key = x value = x }`
 * - 定义类型信息：`## definition_type = x`, `## definition_type = x, y, z`
 * - 覆盖方式：`## override_strategy = x`
 * - 规则表达式：`## config_expression = x`, `## config_expression = { key = x value = y }`
 * - 作用域上下文：`## scope_context = { this = x root = y }`
 */
class ParadoxScriptTextAnnotatedRenderer : ParadoxScriptTextRenderer<ParadoxScriptTextAnnotatedRenderer.Context, String> {
    data class Context(
        var builder: StringBuilder = StringBuilder()
    )

    var level: ParadoxRendererAnnotatedLevel = ParadoxRendererAnnotatedLevel.DEFAULT

    override fun initContext(): Context {
        return Context()
    }

    override fun render(input: PsiElement, context: Context): String {
        return when (input) {
            is ParadoxScriptFile -> render(input, context)
            is ParadoxScriptMember -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxScriptFile, context: Context = initContext()): String {
        val plain = ParadoxScriptTextPlainRenderer()
        val body = plain.render(element)
        context.builder.append(body)
        return context.builder.toString()
    }

    fun render(element: ParadoxScriptMember, context: Context = initContext()): String {
        ProgressManager.checkCanceled()
        renderHeader(element, context)
        val plain = ParadoxScriptTextPlainRenderer()
        val body = plain.render(element)
        if (context.builder.isNotEmpty() && body.isNotEmpty()) {
            context.builder.append("\n\n")
        }
        context.builder.append(body)
        return context.builder.toString()
    }

    private fun renderHeader(element: ParadoxScriptMember, context: Context) {
        val header = buildList {
            if (level.includeType) {
                val typeText = getTypeText(element)
                typeText?.let { add("## type = $it") }
            }
            if (level.includeDefinitionType) {
                val definitionTypeText = getDefinitionTypeText(element)
                definitionTypeText?.let { add("## definition_type = $it") }
            }
            if (level.includeOverrideStrategy) {
                val overrideStrategyText = getOverrideStrategyText(element)
                overrideStrategyText?.let { add("## override_strategy = $it") }
            }
            if (level.includeConfigExpression) {
                val configExpressionText = getConfigExpressionText(element)
                configExpressionText?.let { add("## config_expression = $it") }
            }
            if (level.includeScopeContext) {
                val scopeContextText = getScopeContextText(element, detailed = level.includeDetailedScopeContext)
                scopeContextText?.let { add("## scope_context = $it") }
            }
        }
        if (header.isEmpty()) return
        header.forEachIndexed { index, line ->
            if (index != 0) context.builder.append('\n')
            context.builder.append(line)
        }
    }

    private fun getTypeText(element: ParadoxScriptMember): String? {
        return when (element) {
            is ParadoxScriptProperty -> {
                val keyType = ParadoxTypeManager.getType(element.propertyKey) ?: ParadoxType.Unknown
                val valueType = element.propertyValue?.let { ParadoxTypeManager.getType(it) } ?: ParadoxType.Unknown
                "{ key = ${keyType.id} value = ${valueType.id} }"
            }
            is ParadoxScriptValue -> {
                val type = ParadoxTypeManager.getType(element) ?: ParadoxType.Unknown
                type.id
            }
            else -> null
        }
    }

    private fun getDefinitionTypeText(element: ParadoxScriptMember): String? {
        if (element !is ParadoxScriptProperty) return null
        return ParadoxTypeManager.getDefinitionType(element.propertyKey)
    }

    private fun getOverrideStrategyText(element: ParadoxScriptMember): String? {
        val key = when (element) {
            is ParadoxScriptProperty -> element.propertyKey
            else -> null
        } ?: return null
        return ParadoxTypeManager.getOverrideStrategy(key)?.id
    }

    private fun getConfigExpressionText(element: ParadoxScriptMember): String? {
        return when (element) {
            is ParadoxScriptProperty -> {
                val keyExpression = ParadoxTypeManager.getConfigExpression(element.propertyKey)?.quoteIfNecessary()
                val valueExpression = element.propertyValue?.let { ParadoxTypeManager.getConfigExpression(it) }?.quoteIfNecessary()
                "{ key = $keyExpression value = $valueExpression }"
            }
            is ParadoxScriptValue -> {
                val valueExpression = ParadoxTypeManager.getConfigExpression(element)?.quoteIfNecessary()
                valueExpression
            }
            else -> null
        }
    }

    private fun getScopeContextText(element: ParadoxScriptMember, detailed: Boolean): String? {
        val target = when (element) {
            is ParadoxScriptProperty -> element.propertyValue ?: element.propertyKey
            is ParadoxScriptValue -> element
            else -> null
        } ?: return null
        val scopeContext = ParadoxTypeManager.getScopeContext(target) ?: return null
        val map = scopeContext.toScopeIdMap(showPrev = detailed)
        if (map.isEmpty()) return null
        return map.entries.joinToString(" ", " {", " }") { "${it.key.quoteIfNecessary()} = ${it.value.quoteIfNecessary()}" }
    }
}
