@file:Suppress("UnstableApiUsage", "UNUSED_PARAMETER")

package icu.windea.pls.lang.documentation

import com.intellij.model.*
import com.intellij.openapi.application.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.presentation.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.core.*

//org.jetbrains.kotlin.idea.k2.codeinsight.quickDoc.KotlinDocumentationTarget

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
        return ParadoxDocumentationManager.computeLocalDocumentation(element, originalElement, hint = true)
    }

    override fun computeDocumentation(): DocumentationResult {
        return DocumentationResult.asyncDocumentation {
            val html = runReadAction { ParadoxDocumentationManager.computeLocalDocumentation(element, originalElement, hint = false) } ?: return@asyncDocumentation null
            DocumentationResult.documentation(html)
        }
    }
}
