package icu.windea.pls.ep.reference

import icu.windea.pls.model.*

abstract class ParadoxReferenceLinkType(
    val prefix: String
) {
    // limited support only
    // e.g.,
    // cwt:stellaris:types/civic_or_origin
    // cwt:stellaris:types/civic_or_origin/origin
    data object CwtConfig : ParadoxReferenceLinkType("cwt:") {
        fun createLink(gameType: ParadoxGameType?, vararg parts: String): String {
            return "$prefix${gameType.id}:${parts.joinToString("/")}"
        }
    }

    // e.g.,
    // pdx.sv:civic_default_random_weight
    // pdx.sv:stellaris:civic_default_random_weight
    data object ScriptedVariable : ParadoxReferenceLinkType("pdx.sv:") {
        fun createLink(name: String) : String {
            return "$prefix$name"
        }

        fun createLink(gameType: ParadoxGameType?, name: String): String {
            return "$prefix${gameType.id}:$name"
        }
    }

    // e.g.,
    // pdx.d:origin_default
    // pdx.d:stellaris:origin_default
    // pdx.d:civic_or_origin.origin/origin_default
    // pdx.d:stellaris:civic_or_origin.origin/origin_default
    data object Definition : ParadoxReferenceLinkType("pdx.d:") {
        fun createLink(name: String, typeExpression: String?) : String {
            if(typeExpression == null) return "$prefix$name"
            return "$prefix$typeExpression/$name"
        }

        fun createLink(gameType: ParadoxGameType?, name: String, typeExpression: String?): String {
            if(typeExpression == null) return "$prefix${gameType.id}:$name"
            return "$prefix${gameType.id}:$typeExpression/$name"
        }
    }

    // e.g.,
    // pdx.l:origin_default
    // pdx.l:stellaris:origin_default
    data object Localisation : ParadoxReferenceLinkType("pdx.l:") {
        fun createLink(name: String) : String {
            return "$prefix$name"
        }

        fun createLink(gameType: ParadoxGameType?, name: String): String {
            return "$prefix${gameType.id}:$name"
        }
    }

    // e.g.,
    // pdx.p:common/governments/civics/00_origins.txt
    // pdx.p:stellaris:common/governments/civics/00_origins.txt
    data object FilePath : ParadoxReferenceLinkType("pdx.p:") {
        fun createLink(path: String) : String {
            return "$prefix$path"
        }

        fun createLink(gameType: ParadoxGameType?, path: String): String {
            return "$prefix${gameType.id}:$path"
        }
    }

    // e.g.,
    // pdx.m:job_soldier_add
    // pdx.m:stellaris:job_soldier_add
    data object Modifier : ParadoxReferenceLinkType("pdx.m:") {
        fun createLink(name: String) : String {
            return "$prefix$name"
        }

        fun createLink(gameType: ParadoxGameType?, name: String): String {
            return "$prefix${gameType.id}:$name"
        }
    }
}
