package icu.windea.pls.lang.util.renderers

import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.lang.codeInsight.ParadoxAnnotatedManager

/**
 * 将 CSV 文本渲染为带注解的文本的渲染器。
 *
 * 说明：
 * - 可以配置详细的注解级别。参见 [ParadoxAnnotatedRendererLevel]。
 *
 * 支持的注解：
 * - 类型信息。参见 [ParadoxAnnotatedManager.getTypeForRow]。
 * - 规则表达式信息。参见 [ParadoxAnnotatedManager.getConfigExpressionForRow]。
 */
class ParadoxCsvTextAnnotatedRenderer : ParadoxCsvTextRenderer<ParadoxCsvTextAnnotatedRenderer.Scope, String>() {
    var level: ParadoxAnnotatedRendererLevel = ParadoxAnnotatedRendererLevel.DEFAULT

    override fun createScope(): Scope {
        return Scope(level)
    }

    open class Scope(
        var level: ParadoxAnnotatedRendererLevel,
    ) : ParadoxCsvTextPlainRenderer.Scope() {
        override fun renderRowElement(element: ParadoxCsvRowElement) {
            renderAnnotations(element)
            super.renderRowElement(element)
        }

        fun renderAnnotations(element: ParadoxCsvRowElement) {
            val annotations = getAnnotations(element)
            if (annotations.isEmpty()) return
            for (line in annotations) {
                builder.append(line)
                builder.append('\n')
            }
        }

        fun getAnnotations(element: ParadoxCsvRowElement): List<String> {
            return buildList {
                if (level.includeType) {
                    ParadoxAnnotatedManager.getTypeForRow(element)?.let { add(it) }
                }
                if (level.includeConfigExpression) {
                    ParadoxAnnotatedManager.getConfigExpressionForRow(element)?.let { add(it) }
                }
            }
        }
    }
}
