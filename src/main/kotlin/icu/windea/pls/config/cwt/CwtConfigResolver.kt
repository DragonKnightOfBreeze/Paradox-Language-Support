package icu.windea.pls.config.cwt

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.cwt.psi.*
import java.util.*

/**
 * Cwt配置文件的解析器。
 */
object CwtConfigResolver {
	fun resolve(file: CwtFile): CwtFileConfig {
		val rootBlock = file.block ?: return CwtFileConfig.empty
		return when {
			rootBlock.isEmpty -> CwtFileConfig.empty
			rootBlock.isObject -> {
				val properties = rootBlock.mapChildOfTypeNotNull(CwtProperty::class.java) { resolveProperty(it, file) }
				CwtFileConfig(emptyPointer(), emptyList(), properties)
			}
			rootBlock.isArray -> {
				val values = rootBlock.mapChildOfType(CwtValue::class.java) { resolveValue(it, file) }
				CwtFileConfig(emptyPointer(), values, emptyList())
			}
			else -> CwtFileConfig.empty
		}
	}
	
	private fun resolveProperty(property: CwtProperty, file: CwtFile): CwtPropertyConfig? {
		val pointer = property.createPointer(file)
		val key = property.propertyName
		val propValue = property.value ?: return null
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtValueConfig>? = null
		var properties: List<CwtPropertyConfig>? = null
		var documentationLines:LinkedList<String>? = null
		var options: LinkedList<CwtOptionConfig>? = null
		var optionValues :LinkedList<CwtOptionValueConfig>? = null
		val separatorType = property.separatorType
		when {
			propValue is CwtBoolean -> booleanValue = propValue.booleanValue
			propValue is CwtInt -> intValue = propValue.intValue
			propValue is CwtFloat -> floatValue = propValue.floatValue
			propValue is CwtString -> stringValue = propValue.stringValue
			propValue is CwtBlock -> when {
				propValue.isEmpty -> {
					values = emptyList()
					properties = emptyList()
				}
				propValue.isObject -> {
					properties = propValue.mapChildOfTypeNotNull(CwtProperty::class.java) { resolveProperty(it, file) }
				}
				propValue.isArray -> {
					values = propValue.mapChildOfType(CwtValue::class.java) { resolveValue(it, file) }
				}
			}
		}
		
		var current: PsiElement = property
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) {
						if(documentationLines == null) documentationLines = LinkedList()
						documentationLines.addFirst(documentationText.text)
					}
				}
				current is CwtOptionComment -> {
					val option = current.option
					if(option != null) {
						if(options == null) options = LinkedList()
						val resolvedOption = resolveOption(option, file)
						if(resolvedOption != null) options.addFirst(resolvedOption)
					} else {
						val optionValue = current.value
						if(optionValue != null) {
							if(optionValues == null) optionValues = LinkedList()
							val resolvedOptionValue = resolveOptionValue(optionValue, file)
							optionValues.addFirst(resolvedOptionValue)
						}
					}
				}
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		
		val documentation = documentationLines?.joinToString("\n")
		val keyExpression = CwtKeyExpression.resolve(key)
		val valueExpression = CwtValueExpression.resolve(stringValue.orEmpty())
		val config = CwtPropertyConfig(
			pointer, key, property.propertyValue, booleanValue, intValue, floatValue, stringValue, values , properties,
			documentation, options, optionValues, separatorType, keyExpression, valueExpression
		)
		values?.forEach { it.parent = config }
		properties?.forEach { it.parent = config }
		return config
	}
	
	private fun resolveValue(value: CwtValue, file: CwtFile): CwtValueConfig {
		val pointer = value.createPointer(file)
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtValueConfig>? = null
		var properties: List<CwtPropertyConfig>? = null
		var documentationLines:LinkedList<String>? = null
		var options: LinkedList<CwtOptionConfig>? = null
		var optionValues :LinkedList<CwtOptionValueConfig>? = null
		
		when {
			value is CwtBoolean -> booleanValue = value.booleanValue
			value is CwtInt -> intValue = value.intValue
			value is CwtFloat -> floatValue = value.floatValue
			value is CwtString -> stringValue = value.stringValue
			value is CwtBlock -> when {
				value.isEmpty -> {
					values = emptyList()
					properties = emptyList()
				}
				value.isObject -> {
					properties = value.mapChildOfTypeNotNull(CwtProperty::class.java) { resolveProperty(it, file) }
				}
				value.isArray -> {
					values = value.mapChildOfType(CwtValue::class.java) { resolveValue(it, file) }
				}
			}
		}
		
		var current: PsiElement = value
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) {
						if(documentationLines == null) documentationLines = LinkedList()
						documentationLines.addFirst(documentationText.text)
					}
				}
				current is CwtOptionComment -> {
					val option = current.option
					if(option != null) {
						if(options == null){
							options = LinkedList()
						} else{
							val resolvedOption = resolveOption(option, file)
							if(resolvedOption != null) options.addFirst(resolvedOption)
						}
					} else {
						val optionValue = current.value
						if(optionValue != null) {
							if(optionValues == null) optionValues = LinkedList()
							val resolvedOptionValue = resolveOptionValue(optionValue, file)
							optionValues.addFirst(resolvedOptionValue)
						}
					}
				}
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		val documentation = documentationLines?.joinToString("\n")
		
		val valueExpression = CwtValueExpression.resolve(stringValue.orEmpty())
		return CwtValueConfig(
			pointer, value.value, booleanValue, intValue, floatValue, stringValue,
			values, properties, documentation, options, optionValues, valueExpression
		)
	}
	
	private fun resolveOption(option: CwtOption, file: CwtFile): CwtOptionConfig? {
		val key = option.optionName
		val optionValue = option.value ?: return null
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtOptionValueConfig>? = null
		var options: List<CwtOptionConfig>? = null
		val separatorType = option.separatorType
		when {
			optionValue is CwtBoolean -> booleanValue = optionValue.booleanValue
			optionValue is CwtInt -> intValue = optionValue.intValue
			optionValue is CwtFloat -> floatValue = optionValue.floatValue
			optionValue is CwtString -> stringValue = optionValue.stringValue
			optionValue is CwtBlock -> when {
				optionValue.isEmpty -> {
					values = emptyList()
					options = emptyList()
				}
				optionValue.isObject -> {
					options = optionValue.mapChildOfTypeNotNull(CwtOption::class.java) { resolveOption(it, file) }
				}
				optionValue.isArray -> {
					values = optionValue.mapChildOfType(CwtValue::class.java) { resolveOptionValue(it, file) }
				}
			}
		}
		return CwtOptionConfig(
			emptyPointer(), key, optionValue.value,
			booleanValue, intValue, floatValue, stringValue, values, options, separatorType
		)
	}
	
	private fun resolveOptionValue(option: CwtValue, file: CwtFile): CwtOptionValueConfig {
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtOptionValueConfig>? = null
		var options: List<CwtOptionConfig>? = null
		when {
			option is CwtBoolean -> {
				booleanValue = option.booleanValue
			}
			option is CwtInt -> {
				intValue = option.intValue
			}
			option is CwtFloat -> {
				floatValue = option.floatValue
			}
			option is CwtString -> {
				stringValue = option.stringValue
			}
			option is CwtBlock -> {
				when {
					option.isEmpty -> {
						values = emptyList()
						options = emptyList()
					}
					option.isObject -> {
						options = option.mapChildOfTypeNotNull(CwtOption::class.java)  { resolveOption(it, file) }
					}
					option.isArray -> {
						values = option.mapChildOfType(CwtValue::class.java) { resolveOptionValue(it, file) }
					}
				}
			}
		}
		return CwtOptionValueConfig(
			emptyPointer(), option.value,
			booleanValue, intValue, floatValue, stringValue, values, options
		)
	}
}