package icu.windea.pls.lang.codeInsight.hints

import com.intellij.openapi.client.ClientSystemInfo
import java.awt.Cursor
import java.awt.event.MouseEvent

object PlsHintsUtil {
    // com.intellij.codeInsight.hints.presentation.PresentationFactory.isControlDown
    @Suppress("UnstableApiUsage")
    fun isControlDown(e: MouseEvent): Boolean = (ClientSystemInfo.isMac() && e.isMetaDown) || e.isControlDown

    fun getHandCursor(): Cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
}
