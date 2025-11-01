@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation

import com.intellij.model.Pointer
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import icu.windea.pls.core.createPointer

// org.jetbrains.kotlin.idea.k2.codeinsight.quickDoc.KotlinDocumentationTarget

class CwtDocumentationTarget(
    val element: PsiElement,
    val originalElement: PsiElement?
) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createPointer()
        val originalElementPtr = originalElement?.createPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            CwtDocumentationTarget(element, originalElementPtr?.dereference())
        }
    }

    override val navigatable: Navigatable?
        get() = element as? Navigatable

    override fun computePresentation(): TargetPresentation {
        return getTargetPresentation(element)
    }

    override fun computeDocumentationHint(): String? {
        return runReadAction { CwtDocumentationManager.computeLocalDocumentation(element, originalElement, hint = true) }
    }

    override fun computeDocumentation(): DocumentationResult {
        return DocumentationResult.asyncDocumentation {
            val html = readAction { CwtDocumentationManager.computeLocalDocumentation(element, originalElement, hint = false) }
            html?.let { DocumentationResult.documentation(it) }
        }
    }
}
