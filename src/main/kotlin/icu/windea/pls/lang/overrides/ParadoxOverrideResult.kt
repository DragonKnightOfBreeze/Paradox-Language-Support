package icu.windea.pls.lang.overrides

import com.intellij.psi.PsiElement

/**
 * 重载结果。
 *
 * 用于检查是否存在对目标的重载以及重载是否正确。
 *
 * @property key 用于一致性检测的键（文件的路径或目标的名字）。
 * @property target 目标（文件、全局封装变量、定义、本地化等）。
 * @property results 包括目标自身在内的重载项。
 * @property overrideStrategy 使用的覆盖方式。
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
data class ParadoxOverrideResult<T: PsiElement>(
    val key: String,
    val target: T,
    val results: List<T>,
    val overrideStrategy: ParadoxOverrideStrategy,
)
