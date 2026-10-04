package icu.windea.pls.config.scopes

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toDelimitedSet
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.core.util.UnaryTemplateInfo

@Suppress("unused")
@Optimized
interface CwtDataExpressionMetadataResolutionScope : CwtFilePathResolutionScope {
    fun String.resolveValue(): String? {
        return this.orNull()?.optimized()
    }

    fun String.resolveIntRange(): IntRangeInfo? {
        return IntRangeInfo.create(this)?.normalize()
    }

    fun String.resolveFloatRange(): FloatRangeInfo? {
        return FloatRangeInfo.create(this)?.normalize()
    }

    /**
     * 解析逗号分隔的一组模板参数。每个模板参数必须为包含唯一占位符的一元模板，否则直接忽略。
     */
    fun String.resolveSnippetTemplates(): List<UnaryTemplateInfo> {
        val items = this.toDelimitedSet()
        if (items.isEmpty()) return emptyList()
        val r = items.mapNotNull { UnaryTemplateInfo.create(it)?.normalize() }
        return r.optimized()
    }

    companion object INSTANCE : CwtDataExpressionMetadataResolutionScope
}
