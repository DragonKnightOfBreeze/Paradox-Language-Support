package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import java.util.*

/**
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
 */
data class  CwtTypeConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val path: String? = null,
	val pathStrict: Boolean = false,
	val pathFile: String? = null,
	val pathExtension: String? = null,
	val nameField: String? = null,
	val nameFromFile: Boolean = false,
	val typePerFile: Boolean = false,
	val unique: Boolean = false,
	val severity: String? = null,    
	val skipRootKey: List<List<String>>? = null,
	val typeKeyFilter: ReversibleList<String>? = null,
	val startsWith: String? = null,
	val graphRelatedTypes: List<String>? = null,
	val subtypes: Map<String, CwtSubtypeConfig> = emptyMap(),
	val localisation: CwtTypeLocalisationConfig? = null
) : CwtConfig<CwtProperty>

