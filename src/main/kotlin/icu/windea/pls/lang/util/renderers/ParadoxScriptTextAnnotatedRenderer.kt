package icu.windea.pls.lang.util.renderers

import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.lang.data.annotated.ParadoxAnnotatedDataFactory
import icu.windea.pls.model.data.annotated.ParadoxAnnotatedData
import icu.windea.pls.model.data.annotated.ParadoxAnnotatedLevel
import icu.windea.pls.model.data.annotated.ParadoxConfigExpressionAnnotatedData
import icu.windea.pls.model.data.annotated.ParadoxOverrideStrategyAnnotatedData
import icu.windea.pls.model.data.annotated.ParadoxScopeContextAnnotatedData
import icu.windea.pls.model.data.annotated.ParadoxTypeAnnotatedData
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 将脚本文本渲染为带注解的文本的渲染器。
 *
 * 说明：
 * - 可以配置详细的注解级别。参见 [ParadoxAnnotatedLevel]。
 *
 * 支持的注解数据：
 * - 类型信息。参见 [ParadoxTypeAnnotatedData.FromMember]。
 * - 规则表达式信息。参见 [ParadoxConfigExpressionAnnotatedData.FromMember]。
 * - 定义类型信息。参见 [ParadoxTypeAnnotatedData]。
 * - 覆盖策略信息。参见 [ParadoxOverrideStrategyAnnotatedData]。
 * - 作用域上下文信息。参见 [ParadoxScopeContextAnnotatedData]。
 */
class ParadoxScriptTextAnnotatedRenderer : ParadoxScriptTextRenderer<String, ParadoxScriptTextAnnotatedRenderContext, ParadoxScriptTextAnnotatedRenderSettings>() {
    override val settings = ParadoxScriptTextAnnotatedRenderSettings()

    override fun createContext() = ParadoxScriptTextAnnotatedRenderContext(settings)
}

class ParadoxScriptTextAnnotatedRenderContext(
    private val settings: ParadoxScriptTextAnnotatedRenderSettings,
) : ParadoxScriptTextPlainRenderContext(settings.toPlainSettings()) {
    override fun renderMember(element: ParadoxScriptMember) {
        renderAnnotationComments(element)
        super.renderIndent()
        super.renderMember(element)
    }

    fun renderAnnotationComments(element: ParadoxScriptMember) {
        val annotations = getAnnotations(element)
        if (annotations.isEmpty()) return
        val m = OnceMarker()
        annotations.forEachFast { annotation ->
            if (m.mark()) super.renderIndent()
            builder.append(annotation.toComment())
            builder.append('\n')
        }
    }

    fun getAnnotations(element: ParadoxScriptMember): List<ParadoxAnnotatedData> {
        return buildList {
            if (settings.level.includeType) {
                ParadoxAnnotatedDataFactory.createType(element)?.let { add(it) }
            }
            if (settings.level.includeConfigExpression) {
                ParadoxAnnotatedDataFactory.createConfigExpression(element)?.let { add(it) }
            }
            if (settings.level.includeDefinitionType) {
                ParadoxAnnotatedDataFactory.createDefinitionType(element)?.let { add(it) }
            }
            if (settings.level.includeOverrideStrategy) {
                ParadoxAnnotatedDataFactory.createOverrideStrategy(element)?.let { add(it) }
            }
            if (settings.level.includeScopeContext) {
                val unchanged = settings.level.includeUnchancedScopeContext
                val detailed = settings.level.includeDetailedScopeContext
                ParadoxAnnotatedDataFactory.createScopeContext(element, unchanged, detailed)?.let { add(it) }
            }
        }
    }
}

data class ParadoxScriptTextAnnotatedRenderSettings(
    var indent: String = "    ",
    var inline: Boolean = false,
    var conditional: Boolean = false,
    var level: ParadoxAnnotatedLevel = ParadoxAnnotatedLevel.DEFAULT,
) : ParadoxScriptTextRenderSettings() {
    fun toPlainSettings() = ParadoxScriptTextPlainRenderSettings(indent = indent, inline = inline, conditional = conditional)
}
