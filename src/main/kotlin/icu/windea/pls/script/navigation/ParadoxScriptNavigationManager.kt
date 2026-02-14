package icu.windea.pls.script.navigation

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.icon
import icu.windea.pls.core.optimized
import icu.windea.pls.core.truncate
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember
import javax.swing.Icon

object ParadoxScriptNavigationManager {
    fun accept(element: PsiElement?, forFile: Boolean = true): Boolean {
        return when (element) {
            null -> false
            is ParadoxScriptFile -> forFile
            is ParadoxScriptProperty -> true
            is ParadoxScriptValue -> element.isBlockMember()
            is ParadoxScriptScriptedVariable -> true
            is ParadoxScriptParameterCondition -> true
            else -> false
        }
    }

    fun getIcon(element: PsiElement): Icon? {
        return getPatchedIcon(element) ?: element.icon
    }

    fun getPatchedIcon(element: PsiElement): Icon? {
        when (element) {
            is ParadoxScriptFile -> {
                run {
                    // 模组描述符文件使用特殊图标
                    if (!element.name.endsWith(".mod", true)) return@run
                    return PlsIcons.FileTypes.ModDescriptor
                }
                run {
                    // 作为定义的文件使用特殊图标
                    val definitionInfo = element.definitionInfo ?: return@run
                    return PlsIcons.Nodes.Definition(definitionInfo.type)
                }
            }
            is ParadoxScriptProperty -> {
                run {
                    // 作为内联脚本用法的属性使用特殊图标
                    if (!ParadoxPsiMatcher.isInlineScriptUsage(element)) return@run
                    return PlsIcons.Nodes.Directive
                }
                run {
                    // 作为定义注入的属性使用特殊图标
                    if (!ParadoxPsiMatcher.isDefinitionInjection(element)) return@run
                    return PlsIcons.Nodes.Directive
                }
                run {
                    // 作为定义的属性使用特殊图标
                    val definitionInfo = element.definitionInfo ?: return@run
                    return PlsIcons.Nodes.Definition(definitionInfo.type)
                }
            }
        }
        return null
    }

    fun getLongPresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptFile -> {
                run {
                    // 定义的名字
                    if (element.name.endsWith(".mod", true)) return@run // 排除模组描述符文件
                    val definitionInfo = element.definitionInfo ?: return@run
                    return definitionInfo.name.or.anonymous()
                }
                element.name
            }
            is ParadoxScriptProperty -> {
                run {
                    // 定义的名字
                    val definitionInfo = element.definitionInfo ?: return@run
                    return definitionInfo.name.or.anonymous()
                }
                element.name
            }
            is ParadoxScriptString -> element.name
            is ParadoxScriptValue -> element.value.formatted()
            is ParadoxScriptScriptedVariable -> "@" + element.name.or.unresolved()
            is ParadoxScriptParameterCondition -> element.conditionExpression?.let { "[$it]" }
            else -> null
        }
    }

    fun getLocationString(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptFile -> {
                run {
                    // 定义的类型信息和显示名称
                    if (element.name.endsWith(".mod", true)) return@run // 排除模组描述符文件
                    val definitionInfo = element.definitionInfo ?: return@run
                    return getDefinitionLocationString(element, definitionInfo)
                }
                null
            }
            is ParadoxScriptProperty -> {
                run {
                    // 内联脚本表达式
                    if (!ParadoxPsiMatcher.isInlineScriptUsage(element)) return@run
                    return ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(element, resolve = true)
                }
                run {
                    // 定义的类型信息和显示名称
                    val definitionInfo = element.definitionInfo ?: return@run
                    return getDefinitionLocationString(element, definitionInfo)
                }
                null
            }
            is ParadoxScriptScriptedVariable -> {
                // 封装变量的值
                element.value?.let { " = $it" }
            }
            else -> null
        }
    }

    private fun getDefinitionLocationString(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String {
        return buildString {
            append(": ").append(definitionInfo.typeText)
            val localizedName = ParadoxDefinitionManager.getLocalizedName(element)
            if (localizedName != null) append(" ").append(localizedName)
        }.optimized() // optimized to optimize memory
    }

    private fun String.formatted(): String {
        return when {
            isEmpty() -> "\"\""
            else -> truncate(PlsInternalSettings.getInstance().textLengthLimitForPresentation)
        }
    }
}
