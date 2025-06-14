@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsProvider

abstract class ParadoxHintsProvider<T : Any> : InlayHintsProvider<T> {
    open val renderIcon = false
    open val renderLocalisation = false
}
