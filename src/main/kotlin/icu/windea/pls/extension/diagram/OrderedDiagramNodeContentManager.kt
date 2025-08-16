package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import java.util.*

/**
 * 让nodeItems基于[DiagramCategory]在[DiagramCategoriesHolder.getContentCategories]中的顺序进行排序，
 * 而非默认的基于[DiagramCategory]的名字进行排序。
 */
abstract class OrderedDiagramNodeContentManager : DiagramNodeContentManager {
    val _enabledCategories: MutableSet<DiagramCategory> = Collections.synchronizedSet(TreeSet(compareBy { contentCategories.indexOf(it) }))

    override fun isCategoryEnabled(category: DiagramCategory): Boolean {
        return _enabledCategories.contains(category)
    }

    override fun setCategoryEnabled(category: DiagramCategory, enabled: Boolean) {
        if (enabled) {
            _enabledCategories.add(category)
        } else {
            _enabledCategories.remove(category)
        }
    }

    override fun getEnabledCategories(): Array<DiagramCategory> {
        return _enabledCategories.toTypedArray()
    }
}
