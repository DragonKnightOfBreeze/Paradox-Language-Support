package icu.windea.pls.model.type

import icu.windea.pls.cwt.psi.CwtMember

/**
 * @see CwtMember
 */
enum class CwtMemberRole(val text: String) {
    Property("property"),
    PropertyValue("property_value"),
    DirectValue("direct_value"),
    OptionValue("option_value"),
    OptionDirectValue("option_direct_value"),
    Other("(other)"),
    ;

    override fun toString() = text
}
