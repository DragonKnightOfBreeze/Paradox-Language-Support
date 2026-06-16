package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * 用于操作脚本中的作用域调用语句（scope call statement）。
 *
 * 显式调用形式（normal form）：
 * ```paradox_script
 * exists = owner
 * owner = { ... }
 * ```
 *
 * 安全调用形式（safe form）：
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
    fun isNormalForm(element: ParadoxScriptProperty): Boolean {
        if (isSecondPropertyOfNormalForm(element)) return true
        if (isExistsPropertyOfNormalForm(element)) return true
        return false
    }

    fun isSafeForm(element: ParadoxScriptProperty): Boolean {
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        return separatorNode.elementType == SAFE_ASSIGN_SIGN || separatorNode.elementType == SAFE_CALL_ASSIGN_SIGN
    }

    /**
     * 将显式调用形式转换为安全调用形式。
     */
    fun convertToSafeForm(element: ParadoxScriptProperty, project: Project, gameType: ParadoxGameType) {
        val existsProperty = getExistsProperty(element) ?: return
        val secondProperty = getSecondProperty(element) ?: return
        if (existsProperty === secondProperty) return

        val valueText = secondProperty.propertyValue?.text ?: return
        val keyText = secondProperty.propertyKey.text

        val safeSeparator = when (gameType) {
            ParadoxGameType.Stellaris -> "? = "
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
     * 将安全调用形式转换为显式调用形式。
     * 注释留在原位不动，仅替换属性并在其上方插入 exists 属性。
     */
    fun convertToNormalForm(element: ParadoxScriptProperty, project: Project, gameType: ParadoxGameType) {
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

    fun getExistsProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
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

    fun getSecondProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isSecondPropertyOfNormalForm(element)) return element
        if (isExistsProperty(element)) {
            val next = findNextProperty(element)
            if (next != null && matchesExistsValue(element, next)) return next
        }
        return null
    }

    fun collectCommentsBetween(first: ParadoxScriptProperty, second: ParadoxScriptProperty): List<PsiComment> {
        val comments = mutableListOf<PsiComment>()
        var current = first.nextSibling
        while (current != null && current !== second) {
            if (current is PsiComment) comments.add(current)
            current = current.nextSibling
        }
        return comments
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
        var next = element.nextSibling
        while (next != null) {
            if (next is ParadoxScriptProperty) return next
            next = next.nextSibling
        }
        return null
    }

    private fun findExistsPropertyBefore(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        var prev = element.prevSibling
        while (prev != null) {
            if (prev is ParadoxScriptProperty) {
                if (isExistsProperty(prev)) return prev
                break
            }
            prev = prev.prevSibling
        }
        return null
    }
}
