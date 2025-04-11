@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.lang.util.data

inline operator fun ParadoxLocalisationData.Locale.component1() = name

inline operator fun ParadoxLocalisationData.PropertyList.component1() = locale
inline operator fun ParadoxLocalisationData.PropertyList.component2() = items

inline operator fun ParadoxLocalisationData.Property.component1() = key
inline operator fun ParadoxLocalisationData.Property.component2() = value
