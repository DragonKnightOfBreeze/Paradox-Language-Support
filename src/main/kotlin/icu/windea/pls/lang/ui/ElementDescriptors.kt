package icu.windea.pls.lang.ui

import icu.windea.pls.model.*

sealed interface ElementDescriptor {
    val name: String
    val editInTemplate: Boolean
    
    fun copyDescriptor(): ElementDescriptor
}

data class ValueDescriptor(
    override var name: String = ""
) : ElementDescriptor {
    override val editInTemplate: Boolean get() = false
    
    override fun copyDescriptor(): ElementDescriptor = copy()
}

data class PropertyDescriptor(
    override var name: String = "",
    var separator: ParadoxSeparatorType = ParadoxSeparatorType.EQUAL,
    var value: String = "",
    val constantValues: List<String> = emptyList()
) : ElementDescriptor {
    val constantValueArray = constantValues.toTypedArray()
    
    override val editInTemplate get() = value.isEmpty()
    
    override fun copyDescriptor(): ElementDescriptor = copy()
}