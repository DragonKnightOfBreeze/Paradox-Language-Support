package icu.windea.pls.ep.codeInsight.hints

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

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
@WithGameTypeEP
interface ParadoxQuickDocTextProvider {
    fun getQuickDocText(element: PsiElement): String?

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
