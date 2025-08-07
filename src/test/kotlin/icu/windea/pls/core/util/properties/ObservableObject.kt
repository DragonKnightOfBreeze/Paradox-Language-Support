@file:Suppress("unused", "CanBeParameter")

package icu.windea.pls.core.util.properties

class ObservableObject(
    var name: String,
    val suffix: String,
    var pals: String
) {
    val displayName: String by ::name.observe { "$it, $suffix" }
    var palSet: Set<String> by ::pals.fromCommandDelimitedString()
}
