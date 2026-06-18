package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.quote
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isChainedForm
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isExplicitForm
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isSafeForm
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.linkNodes
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
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
 * 示例 - 显式形式：
 *
 * ```paradox_script
 * exists = owner
 * owner = { k = v }
 * ```
 *
 * 示例 - 安全形式：
 *
 * ```paradox_script
 * # [CK3/VIC3/EU5]
 * owner ?= { k = v }
 *
 * # [Stellaris]
 * owner? = { k = v }
 * ```
 *
 * 示例 - 链式形式：
 *
 * ```paradox_script
 * root.owner = { k = v }
 * ```
 *
 * 示例 - 嵌套形式：
 *
 * ```paradox_script
 * root = {
 *     owner = { k = v }
 * }
 * ```
 */
object ParadoxScopeCallStatementManipulationService {
    /**
     * 判断 [element] 是否为显式形式。
     *
     * 说明：
     * - [element] 默认可以是其中的任一属性（`exists = x` 或 `x = y`）。
     * - [element] 的属性键（作为 `exists` 属性时）/属性值（作为目标属性时）必须是字符串字面量。
     * - `exists = x` 必须在 `x = y` 之前，且两者之间仅能有空白或注释。
     */
    fun isExplicitForm(element: ParadoxScriptProperty, forExistsProperty: Boolean = true, forTargetProperty: Boolean = true): Boolean {
        if (forExistsProperty && isExistsPropertyOfExplicitForm(element)) return true
        if (forTargetProperty && isTargetPropertyOfExplicitForm(element)) return true
        return false
    }

    /**
     * 判断 [element] 是否为安全形式。
     *
     * 说明：
     * - [element] 必须使用 `?=` 或 `? =` 作为分隔符。
     * - [element] 的属性键必须是字符串字面量。
     */
    fun isSafeForm(element: ParadoxScriptProperty): Boolean {
        return isPropertyOfSafeForm(element)
    }

    /**
     * 判断 [element] 是否可以转换为显式形式。
     *
     * 说明：
     * - 对于任意游戏类型和任意安全调用运算符均可用。
     * - [element] 必须是安全形式。参见 [isSafeForm]。
     */
    fun canConvertToExplicitForm(element: ParadoxScriptProperty): Boolean {
        return isSafeForm(element)
    }

    /**
     * 判断 [element] 是否可以转换为安全形式。
     *
     * 说明：
     * - 适用于支持安全（调用）赋值运算符的游戏类型（CK3/VIC3/EU5 使用 `?=`，Stellaris 使用 `? =`）。
     * - [element] 必须是显式形式。参见 [isExplicitForm]。
     */
    fun canConvertToSafeForm(element: ParadoxScriptProperty, canBeExistsProperty: Boolean = true, canBeTargetProperty: Boolean = true): Boolean {
        if (!isExplicitForm(element, canBeExistsProperty, canBeTargetProperty)) return false
        val gameType = ParadoxAnalysisManager.selectGameType(element)
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return ParadoxSyntaxConstraint.SafeAssignOperator.test(gameType) || ParadoxSyntaxConstraint.SafeCallAssignOperator.test(gameType)
    }

    /**
     * 将 [element] 从安全形式转换为显式形式。
     *
     * 示例：
     *
     * ```paradox_script
     * # [CK3/VIC3/EU5] before
     * owner ?= { ... }
     *
     * # [Stellaris] before
     * owner? = { ... }
     *
     * # after
     * exists = owner
     * owner = { ... }
     * ```
     *
     * 说明：
     * - 替换属性文本并在其上方插入 `exists = x` 属性。
     * - 如果在多行块中，会在 `exists = x` 和 `x = y` 之间自动插入换行。
     */
    fun convertToExplicitForm(element: ParadoxScriptProperty, project: Project) {
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
        val firstProperty = ParadoxScriptElementFactory.createProperty(project, firstText)
        val secondProperty = ParadoxScriptElementFactory.createProperty(project, secondText)

        // 用第二属性替换原元素，然后在前面插入 exists
        val replaced = element.replace(secondProperty)
        parent.addBefore(firstProperty, replaced)
        if (needsNewline) {
            val newline = ParadoxScriptElementFactory.createLine(project)
            parent.addBefore(newline, replaced)
        }
    }

    /**
     * 将 [element] 从显式形式转换为安全形式。
     *
     * 示例：
     *
     * ```paradox_script
     * # before
     * exists = owner
     * # comment
     * owner = { ... }
     *
     * # [CK3/VIC3/EU5] after
     * # comment
     * owner ?= { ... }
     *
     * # [Stellaris] after
     * # comment
     * owner? = { ... }
     * ```
     *
     * 说明：
     * - [element] 可以是其中的任一属性（`exists = x` 或 `x = y`）。
     * - `exists = x` 和 `x = y` 之间的注释会保留，并移到转换后的语句之前。
     */
    fun convertToSafeForm(element: ParadoxScriptProperty, project: Project) {
        val existsProperty = getExistsProperty(element) ?: return
        val targetProperty = getTargetProperty(element) ?: return
        if (existsProperty === targetProperty) return

        val valueText = targetProperty.propertyValue?.text ?: return
        val keyText = targetProperty.propertyKey.text

        val gameType = ParadoxAnalysisManager.selectGameType(element)
        if (gameType == null || gameType == ParadoxGameType.Core) return
        val safeSeparator = when {
            ParadoxSyntaxConstraint.SafeCallAssignOperator.test(gameType) -> "? = "
            else -> " ?= "
        }

        // 移动注释到 existsProperty 前面
        val parent = existsProperty.parent
        // 从最后一个注释开始移动，确保顺序正确
        val commentsBetween = PsiService.collectCommentsBetween(targetProperty, existsProperty, forward = false)
        for (comment in commentsBetween) {
            parent.addBefore(comment, existsProperty)
        }

        // 删除 existsProperty 和 targetProperty 之间的所有内容（此时注释已经移走，只余留空白和 targetProperty）
        val firstToDelete = existsProperty.nextSibling
        if (firstToDelete != null && firstToDelete !== targetProperty) {
            parent.deleteChildRange(firstToDelete, targetProperty)
        } else {
            // 直接删除 targetProperty（两者相邻，中间无内容）
            targetProperty.delete()
        }

        // 构建安全形式属性文本（仅属性本身）
        val safeText = keyText + safeSeparator + valueText

        // 用安全形式属性替换 existsProperty
        val newElement = ParadoxScriptElementFactory.createProperty(project, safeText)
        existsProperty.replace(newElement)
    }

    // region Helpers for Normal/Safe Form

    private fun isExistsProperty(element: ParadoxScriptProperty): Boolean {
        if (element.name != "exists") return false
        if (element.propertyValue !is ParadoxScriptString) return false
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        if (separatorNode.elementType != EQUAL_SIGN) return false
        return true
    }

    private fun isTargetProperty(element: ParadoxScriptProperty): Boolean {
        if (element.name == "exists") return false
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        if (separatorNode.elementType != EQUAL_SIGN) return false
        return true
    }

    private fun isExistsPropertyOfExplicitForm(element: ParadoxScriptProperty): Boolean {
        if (!isExistsProperty(element)) return false
        val next = findTargetPropertyAfter(element) ?: return false
        return matchesExistsValue(element, next)
    }

    private fun isTargetPropertyOfExplicitForm(element: ParadoxScriptProperty): Boolean {
        if (!isTargetProperty(element)) return false
        val prev = findExistsPropertyBefore(element) ?: return false
        return matchesExistsValue(prev, element)
    }

    private fun isPropertyOfSafeForm(element: ParadoxScriptProperty): Boolean {
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        if (separatorNode.elementType !in ParadoxScriptTokenSets.SAFE_OPERATOR_TOKENS) return false
        return ParadoxSyntaxService.isSafeOperatorAllowed(element)
    }

    private fun matchesExistsValue(existsProperty: ParadoxScriptProperty, targetProperty: ParadoxScriptProperty): Boolean {
        val propertyValue = existsProperty.propertyValue ?: return false
        if (!ParadoxSyntaxService.isSafeOperatorAllowed(propertyValue)) return false
        val propertyKey = targetProperty.propertyKey
        if (!ParadoxSyntaxService.isSafeOperatorAllowed(propertyKey)) return false
        return propertyKey.value == propertyValue.value
    }

    private fun getExistsProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isExistsPropertyOfExplicitForm(element)) return element
        if (isTargetProperty(element)) {
            val before = findExistsPropertyBefore(element)
            if (before != null && matchesExistsValue(before, element)) return before
        }
        return null
    }

    private fun getTargetProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isTargetPropertyOfExplicitForm(element)) return element
        if (isExistsProperty(element)) {
            val next = findTargetPropertyAfter(element)
            if (next != null && matchesExistsValue(element, next)) return next
        }
        return null
    }

    private fun findTargetPropertyAfter(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        // 跳过（且仅能跳过）空白和注释
        return element.siblings(forward = true, withSelf = false).dropWhile { it is PsiWhiteSpace || it is PsiComment }
            .firstOrNull()?.castOrNull<ParadoxScriptProperty>()?.takeIf { isTargetProperty(it) }
    }

    private fun findExistsPropertyBefore(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        // 跳过（且仅能跳过）空白和注释
        return element.siblings(forward = false, withSelf = false).dropWhile { it is PsiWhiteSpace || it is PsiComment }
            .firstOrNull()?.castOrNull<ParadoxScriptProperty>()?.takeIf { isExistsProperty(it) }
    }

    // endregion

    /**
     * 判断 [element] 是否为嵌套形式。
     *
     * 说明：
     * - 作为外层属性的 [element] 的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]）。
     * - 内层属性的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]）。
     * - 块内的内层属性前后仅允许空白和注释。
     *
     * @see ParadoxLinkedExpression
     */
    fun isNestedForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return false

        val innerProperty = element.properties().singleOrNull() ?: return false
        val innerPropertyKey = innerProperty.propertyKey
        val innerComplexExpression = ParadoxComplexExpression.resolve(innerPropertyKey, configGroup)
        if (innerComplexExpression !is ParadoxLinkedExpression) return false

        return true
    }

    /**
     * 判断 [element] 是否为链式形式。
     *
     * 说明：
     * - [element] 的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]），且含有至少2个链接节点（[ParadoxLinkNode]）。
     *
     * @see ParadoxLinkedExpression
     */
    fun isChainedForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return false
        val linkNodes = complexExpression.linkNodes
        if (linkNodes.size <= 1) return false

        return true
    }

    /**
     * 判断 [element] 是否可以转换为嵌套形式。
     *
     * 说明：
     * - [element] 必须是链式形式。参见 [isChainedForm]。
     *
     * @see ParadoxLinkedExpression
     */
    fun canConvertToNestedForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        return isChainedForm(element, gameType)
    }

    /**
     * 判断 [element] 是否可以转换为链式形式。
     *
     * 说明：
     * - [element] 必须是嵌套形式。参见 [isSafeForm]。
     *
     * @see ParadoxLinkedExpression
     */
    fun canConvertToChainedForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        return isNestedForm(element, gameType)
    }

    /**
     * 将 [element] 从链式形式转换为嵌套形式（单步展开）。
     *
     * 示例（使用 `__` 表示光标位置）：
     *
     * ```paradox_script
     * # before
     * __root.owner = { a = 1 }
     *
     * # after
     * __root = {
     *     owner = { a = 1 }
     * }
     * ```
     *
     * ```paradox_script
     * # before
     * root.__owner = { a = 1 }
     *
     * # after
     * root = {
     *     __owner = { a = 1 }
     * }
     * ```
     *
     * ```paradox_script
     * # before
     * root.__owner.event_target:x
     *
     * # after
     * root = {
     *     __owner.event_target:x
     * }
     * ```
     *
     * 说明：
     * - 找到链式表达式（[ParadoxLinkedExpression]）的直接子节点中，[caretOffset] 之前最后一个（或者链接中第一个）作为分隔符的点号，在此处分隔，
     * - 如果 [element] 的属性键用双引号包围，转换后也保留。反之亦然。
     * - 总是会在 `{` 之后和 `}` 之前自动插入换行。
     * - 如果返回值为正数，则为转换后需要移动到的光标位置的偏移。
     *
     * @see ParadoxLinkedExpression
     */
    fun convertToNestedForm(element: ParadoxScriptProperty, project: Project, caretOffset: Int, gameType: ParadoxGameType? = selectGameType(element)): Int {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return -1
        val linkNodes = complexExpression.linkNodes
        if (linkNodes.size <= 1) return -1

        val expressionOffset = ParadoxExpressionManager.getExpressionOffset(propertyKey)
        val cursorOffsetInExpression = caretOffset - propertyKey.startOffset - expressionOffset

        val outerKey: String
        val innerKey: String
        val moveToInInnerExpression: Int

        // 语义级别分割：基于复杂表达式节点
        val lastMarkerBeforeCursor = complexExpression.nodes.findLast { it is ParadoxOperatorNode && it.rangeInExpression.startOffset < cursorOffsetInExpression }
        if (lastMarkerBeforeCursor != null) {
            val splitStart = lastMarkerBeforeCursor.rangeInExpression.startOffset
            val splitEnd = lastMarkerBeforeCursor.rangeInExpression.endOffset
            outerKey = complexExpression.text.substring(0, splitStart).trimEnd()
            innerKey = complexExpression.text.substring(splitEnd).trimStart()
            moveToInInnerExpression = cursorOffsetInExpression - (complexExpression.rangeInExpression.endOffset - innerKey.length)
        } else {
            val firstLinkEnd = linkNodes[0].rangeInExpression.endOffset
            val secondLinkStart = linkNodes[1].rangeInExpression.startOffset
            outerKey = complexExpression.text.substring(0, firstLinkEnd).trimEnd()
            innerKey = complexExpression.text.substring(secondLinkStart).trimStart()
            moveToInInnerExpression = -1
        }

        val wasQuoted = propertyKey.text.isLeftQuoted()
        val newOuterKeyText = if (wasQuoted) outerKey.quote() else outerKey
        val newInnerKeyText = if (wasQuoted) innerKey.quote() else innerKey
        val newValueText = element.propertyValue?.text.orEmpty() // property value can be null here
        val newText = "$newOuterKeyText = {\n$newInnerKeyText = $newValueText\n}"
        val newElement = ParadoxScriptElementFactory.createProperty(project, newText)
        val replacedElement = element.replace(newElement)

        // 如果有必要，准备移动光标位置
        run {
            if (moveToInInnerExpression < 0) return@run
            if (replacedElement !is ParadoxScriptProperty) return@run
            val document = replacedElement.containingFile?.fileDocument ?: return@run
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document) // 提交文档更改（这会重新格式化更改内容）
            val moveTo = replacedElement.properties().first().startOffset + moveToInInnerExpression + (if (wasQuoted) 1 else 0)
            return moveTo
        }

        return -1
    }

    /**
     * 将 [element] 从嵌套形式转换为链式形式（单步合并）。
     *
     * 示例：
     *
     * ```paradox_script
     * # before
     * root = {
     *     # comment
     *     owner = { a = 1 }
     * }
     *
     * # after
     * # comment
     * root.owner = { a = 1 }
     * ```
     *
     * 说明：
     * - 如果作为外层属性的 [element] 的属性键用双引号包围，转换后也保留。反之亦然。
     * - ~~内层属性前后的注释会保留，并移到转换后的语句之前。~~
     *
     * @see ParadoxLinkedExpression
     */
    fun convertToChainedForm(element: ParadoxScriptProperty, project: Project, gameType: ParadoxGameType? = selectGameType(element)) {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return

        val innerProperty = element.properties().singleOrNull() ?: return
        val innerPropertyKey = innerProperty.propertyKey
        val innerComplexExpression = ParadoxComplexExpression.resolve(innerPropertyKey, configGroup)
        if (innerComplexExpression !is ParadoxLinkedExpression) return

        val outerKey = propertyKey.value
        val innerKey = innerProperty.propertyKey.value

        val newKey = "$outerKey.$innerKey"
        val wasQuoted = propertyKey.text.isLeftQuoted()
        val newKeyText = if (wasQuoted) newKey.quote() else newKey
        val newValueText = innerProperty.propertyValue?.text.orEmpty() // property value can be null here
        val newText = "$newKeyText = $newValueText"
        val newElement = ParadoxScriptElementFactory.createProperty(project, newText)
        element.replace(newElement)
    }
}
