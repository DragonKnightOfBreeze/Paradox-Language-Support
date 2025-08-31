@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.FromOption
import icu.windea.pls.config.config.delegated.FromProperty
import icu.windea.pls.config.config.delegated.impl.CwtTypeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtFilePathMatchableConfig {
    @FromKey("type[$]")
    val name: String
    @FromProperty("base_type: string?")
    val baseType: String?
    @FromProperty("name_field: string?")
    val nameField: String?
    @FromProperty("type_key_prefix: string?")
    val typeKeyPrefix: String?
    @FromProperty("name_from_file: boolean", defaultValue = "false")
    val nameFromFile: Boolean
    @FromProperty("type_per_file: boolean", defaultValue = "false")
    val typePerFile: Boolean
    @FromProperty("unique: boolean", defaultValue = "false")
    val unique: Boolean
    @FromProperty("severity: string?")
    val severity: String?
    @FromProperty("skip_root_key: string | string[]", multiple = true)
    val skipRootKey: List<List<@CaseInsensitive String>>?
    @FromOption("type_key_filter: string | string[]")
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    @FromOption("type_key_regex: string?")
    val typeKeyRegex: Regex?
    @FromOption("starts_with: string?")
    val startsWith: @CaseInsensitive String?
    @FromOption("graph_related_types: string[]")
    val graphRelatedTypes: Set<String>?
    @FromProperty("subtype[*]: SubtypeInfo", multiple = true)
    val subtypes: Map<String, CwtSubtypeConfig>
    @FromProperty("localisation: LocalisationInfo")
    val localisation: CwtTypeLocalisationConfig?
    @FromProperty("images: ImagesInfo")
    val images: CwtTypeImagesConfig?

    val possibleRootKeys: Set<String>
    val typeKeyPrefixConfig: CwtValueConfig? // #123

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtTypeConfig?
    }

    companion object : Resolver by CwtTypeConfigResolverImpl()
}
