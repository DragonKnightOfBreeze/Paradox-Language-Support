package icu.windea.pls.core.codeInsight.documentation

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement

/**
 * 快速文档的通用上下文。
 *
 * 该类型位于 core 层，只依赖平台 API；图片渲染等可选数据通过 userData 附加。
 */
open class DocumentationContext(
    val element: PsiElement,
    val originalElement: PsiElement?,
    val hint: Boolean,
) : UserDataHolderBase() {
    val project: Project get() = element.project
}
