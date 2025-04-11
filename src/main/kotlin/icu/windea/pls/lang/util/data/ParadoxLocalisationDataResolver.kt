package icu.windea.pls.lang.util.data

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化数据的解析器。
 */
object ParadoxLocalisationDataResolver {
    //TODO 1.3.33

    //region Resolve Methods

    /**
     * 解析本地化文件，返回本地化属性列表的数据。
     *
     * 注意：一个本地化文件中可以存在多个本地化属性列表，因此这个方法只会返回文件中的第一个的数据。
     */
    fun resolve(file: PsiFile): ParadoxLocalisationData.PropertyList {
        TODO()
    }

    /**
     * 解析本地化文件，返回一组本地化属性列表的数据。
     */
    fun resolveAll(file: PsiFile): List<ParadoxLocalisationData.PropertyList> {
        TODO()
    }

    fun resolveLocale(element: ParadoxLocalisationLocale): ParadoxLocalisationData.Locale {
        return LocaleImpl(element)
    }

    //endregion

    //region Implementations

    private open class PropertyListImpl1(
        element: ParadoxLocalisationPropertyList
    ) : ParadoxLocalisationData.PropertyList {
        override val element: ParadoxLocalisationPropertyList? get() = null
        override val locale: ParadoxLocalisationData.Locale? = element.locale?.let { LocaleImpl(it) }
        override val items: List<ParadoxLocalisationData.Property> = element.propertyList.map { PropertyImpl(it) }.optimized()
    }

    private class PropertyListImpl2(
        element: ParadoxLocalisationPropertyList,
        file: PsiFile = element.containingFile
    ): PropertyListImpl1(element) {
        private val pointer = element.createPointer(file)

        override val element: ParadoxLocalisationPropertyList? get() = pointer.element
    }

    private class LocaleImpl(
        element: ParadoxLocalisationLocale,
        file: PsiFile = element.containingFile
    ) : ParadoxLocalisationData.Locale {
        private val pointer = element.createPointer(file)

        override val element: ParadoxLocalisationLocale? get() = pointer.element
        override val name: String = element.name
    }

    private class PropertyImpl(
        element: ParadoxLocalisationProperty,
        file: PsiFile = element.containingFile
    ) : ParadoxLocalisationData.Property {
        private val pointer = element.createPointer(file)

        override val element: ParadoxLocalisationProperty? get() = pointer.element
        override val key: String = element.name
        override val value: String? = element.value
    }

    //endregion
}
