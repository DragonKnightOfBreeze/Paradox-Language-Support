package icu.windea.pls.ep.codeInsight.hints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.documentation.ParadoxDocumentationManager
import icu.windea.pls.lang.documentation.ParadoxDocumentationTarget
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.lang.util.ParadoxPsiMatcher
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于为各种目标提供提示文本，并渲染到快速文档中。
 *
 * 目前仅支持以下类型的目标：
 *
 * - 封装变量（[ParadoxScriptScriptedVariable]）
 * - 定义（[ParadoxScriptDefinitionElement]）
 * - 复杂枚举（[ParadoxComplexEnumValueElement]）
 * - 动态值（[ParadoxDynamicValueElement]）
 * - 参数（[ParadoxParameterElement]）
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

        fun getQuickDocText(element: PsiElement): String? {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.reversed().firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getQuickDocText(element)?.orNull()
            }
        }

        fun listQuickDocText(element: PsiElement): List<String> {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.mapNotNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getQuickDocText(element)?.orNull()
            }
        }
    }
}
