package icu.windea.pls.lang.util.renderers

import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.lang.codeInsight.annotated.ParadoxAnnotatedLevel
import icu.windea.pls.lang.codeInsight.annotated.ParadoxScriptAnnotatedManager
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 将脚本文本渲染为带注解的文本的渲染器。
 *
 * 说明：
 * - 可以配置详细的注解级别。参见 [ParadoxAnnotatedLevel]。
 *
 * 支持的注解：
 * - 类型信息。参见 [ParadoxScriptAnnotatedManager.getType]。
 * - 定义类型信息。参见 [ParadoxScriptAnnotatedManager.getDefinitionType]。
 * - 覆盖方式信息。参见 [ParadoxScriptAnnotatedManager.getOverrideStrategy]。
 * - 规则表达式信息。参见 [ParadoxScriptAnnotatedManager.getConfigExpression]。
 * - 作用域上下文信息。参见 [ParadoxScriptAnnotatedManager.getScopeContext]。
 */
class ParadoxScriptTextAnnotatedRenderer : ParadoxScriptTextRenderer<String, ParadoxScriptTextAnnotatedRenderContext, ParadoxScriptTextAnnotatedRenderSettings>() {
    override val settings = ParadoxScriptTextAnnotatedRenderSettings()

    override fun createContext() = ParadoxScriptTextAnnotatedRenderContext(settings)
}

class ParadoxScriptTextAnnotatedRenderContext(
    private val settings: ParadoxScriptTextAnnotatedRenderSettings,
) : ParadoxScriptTextPlainRenderContext(settings.toPlainSettings()) {
    override fun renderMember(element: ParadoxScriptMember) {
        renderAnnotations(element)
        super.renderIndent()
        super.renderMember(element)
    }

    fun renderAnnotations(element: ParadoxScriptMember) {
        val annotations = getAnnotations(element)
        if (annotations.isEmpty()) return
        val m = OnceMarker()
        for (annotation in annotations) {
            if (m.mark()) super.renderIndent()
            builder.append(annotation)
            builder.append('\n')
        }
    }

    fun getAnnotations(element: ParadoxScriptMember): List<String> {
        return buildList {
            if (settings.level.includeType) {
                ParadoxScriptAnnotatedManager.getType(element)?.let { add(it) }
            }
            if (settings.level.includeDefinitionType) {
                ParadoxScriptAnnotatedManager.getDefinitionType(element)?.let { add(it) }
            }
            if (settings.level.includeOverrideStrategy) {
                ParadoxScriptAnnotatedManager.getOverrideStrategy(element)?.let { add(it) }
            }
            if (settings.level.includeConfigExpression) {
                ParadoxScriptAnnotatedManager.getConfigExpression(element)?.let { add(it) }
            }
            if (settings.level.includeScopeContext) {
                val detailed = settings.level.includeDetailedScopeContext
                ParadoxScriptAnnotatedManager.getScopeContext(element, detailed)?.let { add(it) }
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
