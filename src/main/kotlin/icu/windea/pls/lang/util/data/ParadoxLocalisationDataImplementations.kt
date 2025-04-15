package icu.windea.pls.lang.util.data

import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

interface ParadoxLocalisationLazyData {
    class PropertyList(
        element: ParadoxLocalisationPropertyList
    ) : ParadoxLocalisationData.PropertyList {
        override val element: ParadoxLocalisationPropertyList = element
        override val locale: ParadoxLocalisationData.Locale? by lazy { element.locale?.let { Locale(it) } }
        override val items: Sequence<ParadoxLocalisationData.Property> by lazy { element.children().filterIsInstance<ParadoxLocalisationProperty>().map { Property(it) } }
    }

    class Locale(
        element: ParadoxLocalisationLocale
    ) : ParadoxLocalisationData.Locale {
        override val element: ParadoxLocalisationLocale = element
        override val name: String by lazy { element.name }
    }

    class Property(
        element: ParadoxLocalisationProperty
    ) : ParadoxLocalisationData.Property {
        override val element: ParadoxLocalisationProperty = element
        override val key: String by lazy { element.name }
        override val value: String? by lazy { element.value }
    }
}
