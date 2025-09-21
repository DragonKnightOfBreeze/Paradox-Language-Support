package icu.windea.pls.lang.codeInsight.markers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.openapi.util.NlsContexts.Separator
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.codeInsight.navigation.ParadoxGotoRelatedItem

/**
 * 所有“相关项”装订线图标提供器的抽象基类。
 *
 * 该基类统一了装订线图标弹窗中的分组名称（见 [getGroup]），以及将一组 [PsiElement] 转换为带分组的
 * [ParadoxGotoRelatedItem] 的便捷方法（见 [createGotoRelatedItem]）。
 *
 * 实现类只需：
 * 1) 实现 [getGroup] 返回用于分组展示的名称；
 * 2) 在收集目标元素后，调用 [createGotoRelatedItem] 以构造可导航的条目。
 *
 * @see ParadoxGotoRelatedItem
 */
abstract class ParadoxRelatedItemLineMarkerProvider : RelatedItemLineMarkerProvider() {
    abstract fun getGroup(): @Separator String

    protected fun createGotoRelatedItem(elements: Collection<PsiElement>): List<ParadoxGotoRelatedItem> {
        return elements.mapTo(mutableListOf()) { ParadoxGotoRelatedItem(it, getGroup()) }
    }
}
