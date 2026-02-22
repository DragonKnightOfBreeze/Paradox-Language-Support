package icu.windea.pls.lang.util.renderers

import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.lang.codeInsight.annotated.ParadoxAnnotatedManager
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 将脚本文本渲染为带注解的文本的渲染器。
 *
 * 说明：
 * - 可以配置详细的注解级别。参见 [ParadoxAnnotatedRendererLevel]。
 *
 * 支持的注解：
 * - 类型信息。参见 [ParadoxAnnotatedManager.getType]。
 * - 定义类型信息。参见 [ParadoxAnnotatedManager.getDefinitionType]。
 * - 覆盖方式信息。参见 [ParadoxAnnotatedManager.getOverrideStrategy]。
 * - 规则表达式信息。参见 [ParadoxAnnotatedManager.getConfigExpression]。
 * - 作用域上下文信息。参见 [ParadoxAnnotatedManager.getScopeContext]。
 */
class ParadoxScriptTextAnnotatedRenderer : ParadoxScriptTextRenderer<ParadoxScriptTextAnnotatedRenderer.Scope, String>() {
    var indent: String = "    "
    var inline: Boolean = false
    var conditional: Boolean = false
    var level: ParadoxAnnotatedRendererLevel = ParadoxAnnotatedRendererLevel.DEFAULT

    override fun createScope(): Scope {
        return Scope(indent, inline, conditional, level)
    }

    open class Scope(
        indent: String = "    ",
        inline: Boolean = false,
        conditional: Boolean = false,
        var level: ParadoxAnnotatedRendererLevel = ParadoxAnnotatedRendererLevel.DEFAULT,
    ) : ParadoxScriptTextPlainRenderer.Scope(indent = indent, inline = inline, conditional = conditional) {
        override fun renderMember(element: ParadoxScriptMember) {
            renderAnnotations(element)
            renderIndent()
            super.renderMember(element)
        }

        fun renderAnnotations(element: ParadoxScriptMember) {
            val annotations = getAnnotations(element)
            if (annotations.isEmpty()) return
            val m = OnceMarker()
            for (annotation in annotations) {
                if (m.mark()) renderIndent()
                builder.append(annotation)
                builder.append('\n')
            }
        }

        fun getAnnotations(element: ParadoxScriptMember): List<String> {
            return buildList {
                if (level.includeType) {
                    ParadoxAnnotatedManager.getType(element)?.let { add(it) }
                }
                if (level.includeDefinitionType) {
                    ParadoxAnnotatedManager.getDefinitionType(element)?.let { add(it) }
                }
                if (level.includeOverrideStrategy) {
                    ParadoxAnnotatedManager.getOverrideStrategy(element)?.let { add(it) }
                }
                if (level.includeConfigExpression) {
                    ParadoxAnnotatedManager.getConfigExpression(element)?.let { add(it) }
                }
                if (level.includeScopeContext) {
                    val detailed = level.includeDetailedScopeContext
                    ParadoxAnnotatedManager.getScopeContext(element, detailed)?.let { add(it) }
                }
            }
        }
    }
}
