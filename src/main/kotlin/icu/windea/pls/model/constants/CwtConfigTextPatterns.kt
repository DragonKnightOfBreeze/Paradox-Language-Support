package icu.windea.pls.model.constants

import icu.windea.pls.core.util.TextPattern

object CwtConfigTextPatterns {
    val type = TextPattern("type[", "]")
    val subtype = TextPattern("subtype[", "]")
    val row = TextPattern("row[", "]")
    val enum = TextPattern("enum[", "]")
    val complexEnum = TextPattern("complex_enum[", "]")
    val enumValue = TextPattern("enum_value[", "]")
    val value = TextPattern("value[", "]")
    val valueSet = TextPattern("value_set[", "]")
    val dynamicValue = TextPattern("dynamic_value[", "]")
    val inline = TextPattern("inline[", "]")
    val singleAliasRight = TextPattern("single_alias_right[", "]")
    val aliasName = TextPattern("alias_name[", "]")
    val aliasMatchLeft = TextPattern("alias_match_left[", "]")
    val aliasKeysField = TextPattern("alias_keys_field[", "]")
    val singleAlias = TextPattern("single_alias[", "]")
    val alias = TextPattern("alias[", "]", ":")
    val definition = TextPattern("<", ">")
}
