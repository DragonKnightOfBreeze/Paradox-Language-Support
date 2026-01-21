package icu.windea.pls.lang.listeners

import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference

/**
 * 当首选语言环境变更时，清空 PSI 引用的解析缓存。
 *
 * 否则，需要解析为本地化的脚本表达式的 PSI 引用（主要是 [ParadoxScriptExpressionPsiReference]）的解析结果不会被正确刷新。
 */
class ParadoxClearResolveCacheOnPreferredLocaleChangedListener : ParadoxPreferredLocaleListener {
    override fun onChange(oldLocale: String, newLocale: String) {
        val projects = ProjectManager.getInstance().openProjects
        for (project in projects) {
            ResolveCache.getInstance(project).clearCache(true)
        }
    }
}
