package icu.windea.pls.core.ui

import java.awt.Cursor

object UiService {
    // com.intellij.codeInsight.hints.presentation.PresentationFactory.isControlDown
    // @Suppress("UnstableApiUsage")
    // fun isControlDown(e: MouseEvent): Boolean = (ClientSystemInfo.isMac() && e.isMetaDown) || e.isControlDown

    fun getHandCursor(): Cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
}
