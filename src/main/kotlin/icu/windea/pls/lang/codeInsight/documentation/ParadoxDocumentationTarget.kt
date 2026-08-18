package icu.windea.pls.lang.codeInsight.documentation

import com.intellij.model.Pointer
import com.intellij.openapi.application.readAction
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.ep.codeInsight.documentation.ParadoxQuickDocTextProvider

// org.jetbrains.kotlin.idea.k2.codeinsight.quickDoc.KotlinDocumentationTarget

/**
 * @see ParadoxDocumentationManager
 * @see ParadoxDocumentationService
 * @see ParadoxQuickDocTextProvider
 */
@Suppress("UnstableApiUsage")
class ParadoxDocumentationTarget(
    val element: PsiElement,
    val originalElement: PsiElement?
) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createPointer()
        val originalElementPtr = originalElement?.createPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            ParadoxDocumentationTarget(element, originalElementPtr?.dereference())
        }
    }

    override val navigatable: Navigatable?
        get() = element as? Navigatable

    override fun computePresentation(): TargetPresentation {
        return getTargetPresentation(element)
    }

    override fun computeDocumentationHint(): String? {
        return runSmartReadAction { ParadoxDocumentationManager.compute(element, originalElement, hint = true)?.toString() }
    }

    override fun computeDocumentation(): DocumentationResult {
        return DocumentationResult.asyncDocumentation {
            readAction { ParadoxDocumentationManager.compute(element, originalElement, hint = false)?.toDocumentation() }
        }
    }
}
