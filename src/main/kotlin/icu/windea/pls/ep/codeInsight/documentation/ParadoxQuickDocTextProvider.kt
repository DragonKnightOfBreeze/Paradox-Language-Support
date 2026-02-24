package icu.windea.pls.ep.codeInsight.documentation

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationManager
import icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationTarget
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于为各种目标提供提示文本，并渲染到快速文档中。
 *
 * 目前仅支持以下类型的目标：
 *
 * - 封装变量（[ParadoxScriptScriptedVariable]）
 * - 定义（[ParadoxDefinitionElement]）
 * - 复杂枚举（[ParadoxComplexEnumValueLightElement]）
 * - 动态值（[ParadoxDynamicValueLightElement]）
 * - 参数（[ParadoxParameterLightElement]）
 *
 * @see ParadoxDocumentationTarget
 * @see ParadoxDocumentationManager
 * @see ParadoxPsiMatcher
 */
@Suppress("unused")
@WithGameTypeEP
interface ParadoxQuickDocTextProvider {
    val source: Source get() = Source.Other

    fun getQuickDocText(element: PsiElement): String?

    /**
     * 提示文本的来源。
     */
    enum class Source {
        /** 未归类 */
        Other,
        /** 来自扩展规则 */
        Extended,
        ;
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxQuickDocTextProvider>("icu.windea.pls.quickDocTextProvider")
    }
}
