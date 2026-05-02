package icu.windea.pls.lang.search.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.model.ParadoxGameType

/**
 * @property selector 查询选择器。
 */
interface ParadoxSearchParameters<T> {
    val selector: ParadoxSearchSelector<T>

    val gameType: ParadoxGameType? get() = selector.gameType
    val project: Project get() = selector.project
    val scope: GlobalSearchScope get() = selector.scope
}
