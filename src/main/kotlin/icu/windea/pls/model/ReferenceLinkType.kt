package icu.windea.pls.model

@Suppress("unused")
abstract class ReferenceLinkType(
    val prefix: String
) {
    /**
     * limited support only
     *
     * e.g.,
     * - `cwt:stellaris:types/civic_or_origin`
     * - `cwt:stellaris:types/civic_or_origin/origin`
     * - `cwt:stellaris:values/some_dynamic_value_type`
     * - `cwt:stellaris:values/some_dynamic_value_type/some_value`
     * - `cwt:stellaris:enums/some_enum`
     * - `cwt:stellaris:enums/some_enum/some_value`
     * - `cwt:stellaris:complex_enums/some_complex_enum`
     * - `cwt:stellaris:complex_enums/some_complex_enum/some_value`
     * - `cwt:stellaris:scopes/some_scope`
     * - `cwt:stellaris:system_scopes/some_system_scope`
     * - `cwt:stellaris:links/some_link`
     * - `cwt:stellaris:localisation_links/some_localisation_link`
     * - `cwt:stellaris:localisation_commands/some_localisation_command`
     * - `cwt:stellaris:modifier_categories/some_modifier_category`
     * - `cwt:stellaris:modifiers/some_modifier`
     */
    data object CwtConfig : ReferenceLinkType("cwt:") {
        fun createLink(category: String, name: String, gameType: ParadoxGameType? = null): String {
            return "$prefix${gameType.id}:$category/$name"
        }

        object Categories {
            const val types = "types"
            const val values = "values"
            const val enums = "enums"
            const val complexEnums = "complex_enums"
            const val scopes = "scopes"
            const val systemScopes = "system_scopes"
            const val links = "links"
            const val localisationLinks = "localisation_links"
            const val localisationCommands = "localisation_commands"
            const val modifierCategories = "modifier_categories"
            const val modifiers = "modifiers"
        }
    }

    /**
     * e.g.,
     * - `pdx.sv:civic_default_random_weight`
     * - `pdx.sv:stellaris:civic_default_random_weight`
     */
    data object ScriptedVariable : ReferenceLinkType("pdx.sv:") {
        fun createLink(name: String, gameType: ParadoxGameType? = null): String {
            if (gameType == null) return "$prefix$name"
            return "$prefix${gameType.id}:$name"
        }
    }

    /**
     * e.g.,
     * - `pdx.d:origin_default`
     * - `pdx.d:stellaris:origin_default`
     * - `pdx.d:civic_or_origin.origin/origin_default`
     * - `pdx.d:stellaris:civic_or_origin.origin/origin_default`
     */
    data object Definition : ReferenceLinkType("pdx.d:") {
        fun createLink(name: String, typeExpression: String? = null, gameType: ParadoxGameType? = null): String {
            val expression = if (typeExpression == null) name else "$name/$typeExpression"
            if (gameType == null) return "$prefix$expression"
            return "$prefix${gameType.id}:$typeExpression"
        }
    }

    /**
     * e.g.,
     * - `pdx.l:origin_default`
     * - `pdx.l:stellaris:origin_default`
     */
    data object Localisation : ReferenceLinkType("pdx.l:") {
        fun createLink(name: String, gameType: ParadoxGameType?): String {
            if (gameType == null) return "$prefix$name"
            return "$prefix${gameType.id}:$name"
        }
    }

    /**
     * e.g.,
     * - `pdx.p:common/governments/civics/00_origins.txt`
     * - `pdx.p:stellaris:common/governments/civics/00_origins.txt`
     */
    data object FilePath : ReferenceLinkType("pdx.p:") {
        fun createLink(path: String, gameType: ParadoxGameType? = null): String {
            if (gameType == null) return "$prefix$path"
            return "$prefix${gameType.id}:$path"
        }
    }

    /**
     * e.g.,
     * - `pdx.m:job_soldier_add`
     * - `pdx.m:stellaris:job_soldier_add`
     */
    data object Modifier : ReferenceLinkType("pdx.m:") {
        fun createLink(name: String, gameType: ParadoxGameType? = null): String {
            if (gameType == null) return "$prefix$name"
            return "$prefix${gameType.id}:$name"
        }
    }
}
