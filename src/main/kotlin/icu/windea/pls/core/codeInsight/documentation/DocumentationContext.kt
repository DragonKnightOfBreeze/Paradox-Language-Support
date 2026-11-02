package icu.windea.pls.core.codeInsight.documentation

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement

/**
 * 快速文档的通用上下文。
 *
 * 可以通过 userData 附加额外的数据，例如图片渲染请求。
 *
 * @see DocumentationTarget
 * @see PsiDocumentationTargetProvider
 */
open class DocumentationContext(
    val element: PsiElement,
    val originalElement: PsiElement?,
    val hint: Boolean,
) : UserDataHolderBase() {
    val project: Project get() = element.project
}
