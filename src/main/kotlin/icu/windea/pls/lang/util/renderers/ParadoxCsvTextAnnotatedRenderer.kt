package icu.windea.pls.lang.util.renderers

import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.lang.codeInsight.annotated.ParadoxAnnotatedLevel
import icu.windea.pls.lang.codeInsight.annotated.ParadoxCsvAnnotatedManager

/**
 * 将 CSV 文本渲染为带注解的文本的渲染器。
 *
 * 说明：
 * - 可以配置详细的注解级别。参见 [ParadoxAnnotatedLevel]。
 *
 * 支持的注解：
 * - 类型信息。参见 [ParadoxCsvAnnotatedManager.getType]。
 * - 规则表达式信息。参见 [ParadoxCsvAnnotatedManager.getConfigExpression]。
 */
class ParadoxCsvTextAnnotatedRenderer : ParadoxCsvTextRenderer<String, ParadoxCsvTextAnnotatedRenderSettings, ParadoxCsvTextAnnotatedRenderContext>() {
    override val settings = ParadoxCsvTextAnnotatedRenderSettings()

    override fun createContext() = ParadoxCsvTextAnnotatedRenderContext(settings)
}

class ParadoxCsvTextAnnotatedRenderContext(
    private val settings: ParadoxCsvTextAnnotatedRenderSettings,
) : ParadoxCsvTextPlainRenderContext(settings.toPlainSettings()) {
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
            if (settings.level.includeType) {
                ParadoxCsvAnnotatedManager.getType(element)?.let { add(it) }
            }
            if (settings.level.includeConfigExpression) {
                ParadoxCsvAnnotatedManager.getConfigExpression(element)?.let { add(it) }
            }
        }
    }
}

data class ParadoxCsvTextAnnotatedRenderSettings(
    var separator: String = ";",
    var level: ParadoxAnnotatedLevel = ParadoxAnnotatedLevel.DEFAULT
) : ParadoxCsvTextRenderSettings() {
    fun toPlainSettings() = ParadoxCsvTextPlainRenderSettings(separator)
}
