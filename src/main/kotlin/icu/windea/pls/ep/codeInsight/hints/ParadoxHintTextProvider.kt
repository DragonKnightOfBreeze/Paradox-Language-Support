package icu.windea.pls.ep.codeInsight.hints

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.hints.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于为各种目标提供提示文本，并渲染到（专门用于渲染提示文本的）内嵌提示中。
 *
 * 目前仅支持以下类型的目标：
 *
 * - 封装变量（[ParadoxScriptScriptedVariable]）
 * - 定义（[ParadoxScriptDefinitionElement]）
 * - 复杂枚举（[ParadoxComplexEnumValueElement]）
 * - 动态值（[ParadoxDynamicValueElement]）
 *
 * @see ParadoxHintsProvider
 * @see ParadoxPsiMatcher
 */
@Suppress("unused")
@WithGameTypeEP
interface ParadoxHintTextProvider {
    val source: Source get() = Source.Other

    /**
     * @param locale 提示文本来自本地化时，优先使用的语言区域，如果为null则优先使用偏好的语言区域。
     */
    fun getHintText(element: PsiElement, locale: CwtLocaleConfig? = null): String?

    /**
     * @param locale 提示文本来自本地化时，优先使用的语言区域，如果为null则优先使用偏好的语言区域。
     */
    fun getHintLocalisation(element: PsiElement, locale: CwtLocaleConfig? = null): ParadoxLocalisationProperty?

    /**
     * 提示文本的来源。
     */
    enum class Source {
        /** 未归类 */
        Other,
        /** 来自同名的本地化 */
        NameLocalisation,
        /** 来自相关的本地化 */
        RelatedLocalisation,
        /** 来自扩展规则 */
        Extended,
        ;
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxHintTextProvider>("icu.windea.pls.hintTextProvider")

        fun getHintText(element: PsiElement): String? {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.reversed().firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getHintText(element)?.orNull()
            }
        }

        fun getHintLocalisation(element: PsiElement): ParadoxLocalisationProperty? {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.reversed().firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getHintLocalisation(element)
            }
        }
    }
}

