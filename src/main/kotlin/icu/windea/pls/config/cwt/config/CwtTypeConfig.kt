package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.annotation.*
import icu.windea.pls.cwt.psi.*

/**
 * @property block (property) block: boolean EXTENDED BY PLS
 * @property path (property) path: string/path
 * @property pathStrict (property) path_strict: boolean
 * @property pathFile (property) path_file: string/fileName
 * @property pathExtension (property) path_extension: string/fileExtension
 * @property nameField (property) name_field: string/propertyKey
 * @property nameFromFile (property) name_from_file: boolean
 * @property typePerFile (property) type_per_file: boolean
 * @property typeKeyFilter (property) type_key_filter: boolean
 * @property unique (property) unique: boolean
 * @property severity (property) severity: string:severity
 * @property skipRootKey (property*) skip_root_key: string | string[]
 * @property typeKeyFilter (option) type_key_filter: string | string[]
 * @property startsWith (option) starts_with: string
 * @property graphRelatedTypes (option) graph_related_types: graphRelatedType[]
 * @property subtypes (property*) subtype[?]: subtypeInfo
 * @property localisation (property*) localisation: localisationInfo
 * @property pictures (property*) pictures: picturesInfo
 */
data class CwtTypeConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val block: Boolean = true, //EXTENDED BY PLS
	val path: String? = null,
	val pathStrict: Boolean = false,
	val pathFile: String? = null,
	val pathExtension: String? = null,
	val nameField: String? = null,
	val nameFromFile: Boolean = false,
	val typePerFile: Boolean = false,
	val unique: Boolean = false,
	val severity: String? = null,
	val skipRootKey: List<List<@CaseInsensitive String>>? = null,
	val typeKeyFilter: ReversibleSet<@CaseInsensitive String>? = null,
	val startsWith: @CaseInsensitive String? = null,
	val graphRelatedTypes: List<String>? = null,
	val subtypes: Map<String, CwtSubtypeConfig> = emptyMap(),
	val localisation: CwtTypeLocalisationConfig? = null,
	val pictures: CwtTypePicturesConfig? = null

) : CwtConfig<CwtProperty>
