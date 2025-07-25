@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.psi.symbols

import com.intellij.platform.backend.presentation.*
import com.intellij.psi.*

fun TargetPresentationBuilder.withLocationIn(file: PsiFile): TargetPresentationBuilder {
    val virtualFile = file.containingFile.virtualFile ?: return this
    val fileType = virtualFile.fileType
    return locationText(virtualFile.name, fileType.icon)
}
