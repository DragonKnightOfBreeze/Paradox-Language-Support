package icu.windea.pls.core.ui

import javax.swing.Icon

/**
 * 委托给原始图标的图标。用于某些特殊场合。
 */
class DelegatedIcon(private val delegate: Icon) : Icon by delegate {
    // NOTE 2.1.6 有时需要使用特殊的 `DelegatedIcon` 来绕过某些地方的检查……这是BUG还是设计如此呢？非常神秘。
    // see: com.intellij.codeInsight.intention.impl.IntentionListStep.getMaxIconSize

    override fun equals(other: Any?): Boolean {
        return this === other || (other is DelegatedIcon && delegate == other.delegate)
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }
}
