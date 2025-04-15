package icu.windea.pls.lang.util.data

import icu.windea.pls.localisation.psi.*

interface ParadoxLocalisationData {
    interface PropertyList {
        val element: ParadoxLocalisationPropertyList
        val locale: Locale?
        val items: Sequence<Property>
    }

    interface Locale {
        val element: ParadoxLocalisationLocale
        val name: String
    }

    interface Property {
        val element: ParadoxLocalisationProperty
        val key: String
        val value: String?
    }
}
