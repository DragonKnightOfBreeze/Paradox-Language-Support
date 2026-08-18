package icu.windea.pls.model.constants

import icu.windea.pls.core.text.TextPattern

@Suppress("unused")
object CwtConfigTextPatterns {
    val type = TextPattern.WithSurrounding("type[", "]")
    val subtype = TextPattern.WithSurrounding("subtype[", "]")
    val row = TextPattern.WithSurrounding("row[", "]")
    val enum = TextPattern.WithSurrounding("enum[", "]")
    val complexEnum = TextPattern.WithSurrounding("complex_enum[", "]")
    val union = TextPattern.WithSurrounding("union[", "]")
    val value = TextPattern.WithSurrounding("value[", "]")
    val valueSet = TextPattern.WithSurrounding("value_set[", "]")
    val dynamicValue = TextPattern.WithSurrounding("dynamic_value[", "]")
    val singleAliasRight = TextPattern.WithSurrounding("single_alias_right[", "]")
    val aliasName = TextPattern.WithSurrounding("alias_name[", "]")
    val aliasMatchLeft = TextPattern.WithSurrounding("alias_match_left[", "]")
    val aliasKeysField = TextPattern.WithSurrounding("alias_keys_field[", "]")
    val singleAlias = TextPattern.WithSurrounding("single_alias[", "]")
    val alias = TextPattern.DelimitedWithSurrounding("alias[", "]", ":")
    val macro = TextPattern.WithSurrounding("macro[", "]")
    val definition = TextPattern.WithSurrounding("<", ">")
}
