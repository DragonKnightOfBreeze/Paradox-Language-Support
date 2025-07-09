package icu.windea.pls.lang.util.data

import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化数据的解析器。
 */
object ParadoxLocalisationDataResolver {
    /**
     * 解析本地化文件，返回本地化属性列表的数据。
     *
     * 注意：一个本地化文件中可以存在多个本地化属性列表，因此这个方法只会返回文件中的第一个的数据。
     */
    fun resolve(file: PsiFile): ParadoxLocalisationData.PropertyList? {
        if (file !is ParadoxLocalisationFile) return null
        return file.propertyLists.firstOrNull()?.let { resolvePropertyList(it) }
    }

    /**
     * 解析本地化文件，返回一组本地化属性列表的数据。
     */
    fun resolveAll(file: PsiFile): List<ParadoxLocalisationData.PropertyList> {
        if (file !is ParadoxLocalisationFile) return emptyList()
        return file.propertyLists.map { resolvePropertyList(it) }
    }

    private fun resolvePropertyList(element: ParadoxLocalisationPropertyList): ParadoxLocalisationLazyData.PropertyList {
        return ParadoxLocalisationLazyData.PropertyList(element)
    }
}

