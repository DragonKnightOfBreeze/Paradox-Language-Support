package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * 用于操作脚本中的作用域调用语句（scope call statement）。
 *
 * 说明：
 * - 对于显式形式，要求 `exists = x` 必须在 `x = { ... }` 之前，且两者之间仅能有空白或注释。
 *
 * 示例 - 显式形式：
 *
 * ```paradox_script
 * exists = owner
 * owner = { ... }
 * ```
 *
 * 示例 - 安全形式）：
 *
 * CK3/VIC3/EU5:
 * ```paradox_script
 * owner ?= { ... }
 * ```
 *
 * Stellaris:
 * ```paradox_script
 * owner? = { ... }
 * ```
 */
object ParadoxScopeCallStatementManipulationService {
    /**
     * 判断 [element] 是否为显式形式（`exists = x x = y`）。
     * [element] 可以是显式形式中的任意一个属性（`exists = x` 或 `x = y`）。
     */
    fun isNormalForm(element: ParadoxScriptProperty, canBeExistsProperty: Boolean = true, canBeSecondProperty: Boolean = true): Boolean {
        if (canBeExistsProperty && isExistsPropertyOfNormalForm(element)) return true
        if (canBeSecondProperty && isSecondPropertyOfNormalForm(element)) return true
        return false
    }

    /**
     * 判断 [element] 是否为安全形式（使用 `?=` 或 `? =` 作为分隔符的属性）。
     */
    fun isSafeForm(element: ParadoxScriptProperty): Boolean {
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        return separatorNode.elementType == SAFE_ASSIGN_SIGN || separatorNode.elementType == SAFE_CALL_ASSIGN_SIGN
    }

    /**
     * 判断是否可以转换为安全形式。
     *
     * 说明：
     * - 必须是显式形式。
     * - 游戏类型必须支持至少一种安全操作符（宽松测试：仅检查游戏类型，不检查游戏版本）。
     * - 仅检查语法级别（键必须是字符串字面量）。
     */
    fun canConvertToSafeForm(element: ParadoxScriptProperty, canBeExistsProperty: Boolean = true, canBeSecondProperty: Boolean = true): Boolean {
        if (!isNormalForm(element, canBeExistsProperty, canBeSecondProperty)) return false
        val secondProperty = getSecondProperty(element) ?: return false
        if (!ParadoxSyntaxService.isSafeAssignOperatorAllowed(secondProperty)) return false
        val gameType = ParadoxAnalysisManager.selectGameType(element)
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return ParadoxSyntaxConstraint.SafeAssignOperator.testTarget(gameType)
            || ParadoxSyntaxConstraint.SafeCallAssignOperator.testTarget(gameType)
    }

    /**
     * 判断是否可以转换为显式形式。
     *
     * 说明：
     * - 对于任意游戏类型和任意安全调用操作符均可用。
     * - 仅检查语法级别（键必须是字符串字面量）。
     */
    fun canConvertToNormalForm(element: ParadoxScriptProperty): Boolean {
        if (!isSafeForm(element)) return false
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        return when (separatorNode.elementType) {
            SAFE_ASSIGN_SIGN -> ParadoxSyntaxService.isSafeAssignOperatorAllowed(element)
            SAFE_CALL_ASSIGN_SIGN -> ParadoxSyntaxService.isSafeCallAssignOperatorAllowed(element)
            else -> false
        }
    }

    /**
     * 将显式形式转换为安全形式。
     *
     * 说明：
     * - 根据 [ParadoxSyntaxConstraint] 来决定使用 `?=` 还是 `? =`。
     * - `exists = x` 和 `x = y` 之间的注释会保留并移到安全形式之前。
     *
     * @param element 显式形式中的任一属性（`exists = x` 或 `x = y`）。
     * @param gameType 游戏类型，用于决定使用 `?=` 还是 `? =`。
     */
    fun convertToSafeForm(element: ParadoxScriptProperty, project: Project, gameType: ParadoxGameType) {
        val existsProperty = getExistsProperty(element) ?: return
        val secondProperty = getSecondProperty(element) ?: return
        if (existsProperty === secondProperty) return

        val valueText = secondProperty.propertyValue?.text ?: return
        val keyText = secondProperty.propertyKey.text

        val safeSeparator = when {
            ParadoxSyntaxConstraint.SafeCallAssignOperator.testTarget(gameType) -> "? = "
            else -> " ?= "
        }

        val commentsBetween = collectCommentsBetween(existsProperty, secondProperty)

        // 移动注释到 existsProperty 前面
        val parent = existsProperty.parent
        // 从最后一个注释开始移动，确保顺序正确
        for (comment in commentsBetween.asReversed()) {
            parent.addBefore(comment, existsProperty)
        }

        // 删除 existsProperty 和 secondProperty 之间的所有内容（此时注释已经移走，只余留空白和 secondProperty）
        val firstToDelete = existsProperty.nextSibling
        if (firstToDelete != null && firstToDelete !== secondProperty) {
            parent.deleteChildRange(firstToDelete, secondProperty)
        } else {
            // 直接删除 secondProperty（两者相邻，中间无内容）
            secondProperty.delete()
        }

        // 构建安全形式属性文本（仅属性本身）
        val safeText = keyText + safeSeparator + valueText

        // 用安全形式属性替换 existsProperty
        val newElement = ParadoxScriptElementFactory.createProperty(project, safeText)
        existsProperty.replace(newElement)
    }

    /**
     * 将安全形式转换为显式形式。
     *
     * 说明：
     * - 注释留在原位不动，仅替换属性并在其上方插入 `exists = x` 属性。
     * - 如果在多行块中，会在两个属性之间自动插入换行。
     *
     * @param element 安全形式的属性（使用 `?=` 或 `? =` 作为分隔符）。
     */
    fun convertToNormalForm(element: ParadoxScriptProperty, project: Project) {
        val keyText = element.propertyKey.text
        val valueText = element.propertyValue?.text ?: return

        val parent = element.parent

        // 判断是否需要换行（如果元素前已有换行，说明外层块是多行的）
        val parentText = parent.text
        val offsetInParent = element.textRange.startOffset - parent.textRange.startOffset
        val needsNewline = parentText.take(offsetInParent).contains('\n')

        // 创建 exists 属性和第二属性
        val firstText = "exists = $keyText"
        val secondText = "$keyText = $valueText"
        val firstProp = ParadoxScriptElementFactory.createProperty(project, firstText)
        val secondProp = ParadoxScriptElementFactory.createProperty(project, secondText)

        // 用第二属性替换原元素，然后在前面插入 exists
        val replaced = element.replace(secondProp)
        parent.addBefore(firstProp, replaced)
        if (needsNewline) {
            val newline = ParadoxScriptElementFactory.createLine(project)
            parent.addBefore(newline, replaced)
        }
    }

    private fun getExistsProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isExistsProperty(element)) {
            val next = findNextProperty(element)
            if (next != null && matchesExistsValue(element, next)) return element
            return null
        }
        var prev = element.prevSibling
        while (prev != null) {
            if (prev is ParadoxScriptProperty) {
                if (isExistsProperty(prev) && matchesExistsValue(prev, element)) return prev
                break
            }
            prev = prev.prevSibling
        }
        return null
    }

    private fun getSecondProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isSecondPropertyOfNormalForm(element)) return element
        if (isExistsProperty(element)) {
            val next = findNextProperty(element)
            if (next != null && matchesExistsValue(element, next)) return next
        }
        return null
    }

    private fun collectCommentsBetween(first: ParadoxScriptProperty, second: ParadoxScriptProperty): List<PsiComment> {
        return first.siblings(withSelf = false).takeWhile { it !== second }.filterIsInstance<PsiComment>().toList()
    }

    private fun isExistsProperty(element: ParadoxScriptProperty): Boolean {
        return element.name == "exists" && element.propertyValue is ParadoxScriptString
    }

    private fun isExistsPropertyOfNormalForm(element: ParadoxScriptProperty): Boolean {
        if (!isExistsProperty(element)) return false
        val next = findNextProperty(element) ?: return false
        return matchesExistsValue(element, next)
    }

    private fun isSecondPropertyOfNormalForm(element: ParadoxScriptProperty): Boolean {
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        if (separatorNode.elementType != EQUAL_SIGN) return false
        val prev = findExistsPropertyBefore(element) ?: return false
        return matchesExistsValue(prev, element)
    }

    private fun matchesExistsValue(existsProperty: ParadoxScriptProperty, targetProperty: ParadoxScriptProperty): Boolean {
        val existsValue = existsProperty.propertyValue?.stringValue() ?: return false
        val targetKey = targetProperty.propertyKey.stringValue()
        return targetKey == existsValue
    }

    private fun findNextProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        // 跳过（且仅能跳过）空白和注释
        return element.siblings(forward = true, withSelf = false).dropWhile { it is PsiWhiteSpace || it is PsiComment }
            .firstOrNull()?.castOrNull()
    }

    private fun findExistsPropertyBefore(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        // 跳过（且仅能跳过）空白和注释
        return element.siblings(forward = false, withSelf = false).dropWhile { it is PsiWhiteSpace || it is PsiComment }
            .firstOrNull()?.castOrNull<ParadoxScriptProperty>()?.takeIf { isExistsProperty(it) }
    }
}
