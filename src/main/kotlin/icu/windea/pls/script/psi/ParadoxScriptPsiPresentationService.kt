package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.icon
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.truncate
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.defineInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.ChronicleInternalSettings
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.model.ParadoxDefineNamespaceInfo
import icu.windea.pls.model.ParadoxDefineVariableInfo
import javax.swing.Icon

object ParadoxScriptPsiPresentationService {
    fun accept(element: PsiElement?, forFile: Boolean = true): Boolean {
        return when (element) {
            null -> false
            is ParadoxScriptFile -> forFile
            is ParadoxScriptProperty -> true
            is ParadoxScriptValue -> element.isDirectValue()
            is ParadoxScriptScriptedVariable -> true
            is ParadoxScriptConditionalBlock -> true
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
                    // 模组描述符文件
                    if (!element.name.endsWith(".mod", true)) return@run
                    return ChronicleIcons.FileTypes.ModDescriptor
                }
                run {
                    // 作为定义的文件
                    val definitionInfo = element.definitionInfo ?: return@run
                    return ChronicleIcons.Nodes.Definition(definitionInfo.type)
                }
            }
            is ParadoxScriptProperty -> {
                run {
                    // 作为内联脚本用法的属性
                    if (!ParadoxPsiMatchService.isInlineScriptUsage(element, selectGameType(element))) return@run
                    return ChronicleIcons.Nodes.Macro
                }
                run {
                    // 作为定义注入的属性
                    if (!ParadoxPsiMatchService.isDefinitionInjectionUsage(element, selectGameType(element))) return@run
                    return ChronicleIcons.Nodes.Macro
                }
                run {
                    // 作为定义的属性
                    val definitionInfo = element.definitionInfo ?: return@run
                    return ChronicleIcons.Nodes.Definition(definitionInfo.type)
                }
                run {
                    // 作为定值的命名空间或变量的属性
                    val defineInfo = element.defineInfo ?: return@run
                    return when (defineInfo) {
                        is ParadoxDefineNamespaceInfo -> ChronicleIcons.Nodes.DefineNamespace
                        is ParadoxDefineVariableInfo -> ChronicleIcons.Nodes.DefineVariable
                    }
                }
            }
            is ParadoxScriptStringExpressionElement -> {
                run {
                    // 作为复杂枚举值的字符串表达式
                    val complexEnumValueInfo = element.complexEnumValueInfo ?: return@run
                    return ChronicleIcons.Nodes.ComplexEnumValue(complexEnumValueInfo.enumName)
                }
            }
        }
        return null
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            // 定义的名字（可能匿名），或者文件的名字
            is ParadoxScriptFile -> {
                run {
                    // 定义的名字
                    if (element.name.endsWith(".mod", true)) return@run // 排除模组描述符文件
                    val definitionInfo = element.definitionInfo ?: return@run
                    return definitionInfo.name.or.anonymous()
                }
                element.name
            }
            // 定义的名字（可能匿名），或者属性的名字
            is ParadoxScriptProperty -> {
                run {
                    // 定义的名字
                    val definitionInfo = element.definitionInfo ?: return@run
                    return definitionInfo.name.or.anonymous()
                }
                element.name
            }
            // 复杂枚举值的名字，或者截断后的名字
            is ParadoxScriptStringExpressionElement -> {
                run {
                    // 复杂枚举值的名字
                    val complexEnumValueInfo = element.complexEnumValueInfo ?: return@run
                    return complexEnumValueInfo.name
                }
                element.value.formatted()
            }
            // 截断后的名字
            is ParadoxScriptValue -> element.value.formatted()
            // 名字
            is ParadoxScriptScriptedVariable -> "@" + element.name.or.unresolved()
            // 表达式
            is ParadoxScriptConditionalBlock -> element.conditionExpression?.let { "[$it]" }
            // 回退
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getTreePresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getLongPresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getLocationString(element: PsiElement): String? {
        ParadoxPsiPresentationService.getFileInfoText(element)?.let { return it }
        return element.containingFile?.name
    }

    fun getTreeLocationString(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptFile -> {
                run {
                    // 定义的类型信息和展示名字
                    if (element.name.endsWith(".mod", true)) return@run // 排除模组描述符文件
                    val definitionInfo = element.definitionInfo ?: return@run
                    return buildString {
                        append(": ").append(definitionInfo.typeText)
                        ParadoxDefinitionManager.getPresentableName(element)?.let { append(" ").append(it) }
                    }.optimized() // optimized to optimize memory
                }
                null
            }
            is ParadoxScriptProperty -> {
                run {
                    // 内联脚本表达式
                    if (!ParadoxPsiMatchService.isInlineScriptUsage(element, selectGameType(element))) return@run
                    return ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(element, resolve = true)
                }
                run {
                    // 定义的类型信息和展示名字
                    val definitionInfo = element.definitionInfo ?: return@run
                    return buildString {
                        append(": ").append(definitionInfo.typeText)
                        ParadoxDefinitionManager.getPresentableName(element)?.let { append(" ").append(it) }
                    }
                }
                null
            }
            is ParadoxScriptStringExpressionElement -> {
                run {
                    // 复杂枚举值的类型信息和展示名字
                    val complexEnumValueInfo = element.complexEnumValueInfo ?: return@run
                    return buildString {
                        append(": ").append(complexEnumValueInfo.enumName)
                        ParadoxComplexEnumValueManager.getPresentableName(complexEnumValueInfo.name, element)?.let { append(" ").append(it) }
                    }
                }
                null
            }
            is ParadoxScriptScriptedVariable -> {
                // 封装变量的值和展示名字
                buildString {
                    element.value?.let { append(" = ").append(it) }
                    ParadoxScriptedVariableManager.getPresentableName(element)?.let { append(" ").append(it) }
                }.orNull()
            }
            else -> null
        }
    }

    private fun String.formatted(): String {
        return when {
            isEmpty() -> "\"\""
            else -> truncate(ChronicleInternalSettings.getInstance().textLengthLimitForPresentation)
        }
    }
}
