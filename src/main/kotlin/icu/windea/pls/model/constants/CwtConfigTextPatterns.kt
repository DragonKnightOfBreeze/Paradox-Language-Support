package icu.windea.pls.model.constants

import icu.windea.pls.core.util.text.TextPattern

@Suppress("unused")
object CwtConfigTextPatterns {
    val type = TextPattern.from("type[", "]")
    val subtype = TextPattern.from("subtype[", "]")
    val row = TextPattern.from("row[", "]")
    val enum = TextPattern.from("enum[", "]")
    val complexEnum = TextPattern.from("complex_enum[", "]")
    val enumValue = TextPattern.from("enum_value[", "]")
    val value = TextPattern.from("value[", "]")
    val valueSet = TextPattern.from("value_set[", "]")
    val dynamicValue = TextPattern.from("dynamic_value[", "]")
    val singleAliasRight = TextPattern.from("single_alias_right[", "]")
    val aliasName = TextPattern.from("alias_name[", "]")
    val aliasMatchLeft = TextPattern.from("alias_match_left[", "]")
    val aliasKeysField = TextPattern.from("alias_keys_field[", "]")
    val singleAlias = TextPattern.from("single_alias[", "]")
    val alias = TextPattern.from("alias[", "]", ":")
    val directive = TextPattern.from("directive[", "]")
    val definition = TextPattern.from("<", ">")
}
