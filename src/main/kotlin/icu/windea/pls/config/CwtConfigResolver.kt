package icu.windea.pls.config

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.cwt.psi.*
import java.util.*

/**
 * Cwt规则的解析器。
 */
object CwtConfigResolver {
	fun resolve(file: CwtFile): CwtFileConfig {
		val rootBlock = file.block ?: return CwtFileConfig.EmptyConfig
		return when {
			rootBlock.isEmpty -> CwtFileConfig.EmptyConfig
			rootBlock.isObject -> {
				val properties = SmartList<CwtPropertyConfig>()
				rootBlock.processChildrenOfType<CwtProperty> { resolveProperty(it, file).addTo(properties).end() }
				CwtFileConfig(emptyPointer(), emptyList(), properties)
			}
			rootBlock.isArray -> {
				val values = SmartList<CwtValueConfig>()
				rootBlock.processChildrenOfType<CwtValue> { resolveValue(it, file).addTo(values).end() }
				CwtFileConfig(emptyPointer(), values, emptyList())
			}
			else -> CwtFileConfig.EmptyConfig
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
		var documentationLines: LinkedList<String>? = null
		var options: LinkedList<CwtOptionConfig>? = null
		var optionValues: LinkedList<CwtOptionValueConfig>? = null
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
					properties = SmartList()
					propValue.processChildrenOfType<CwtProperty> { resolveProperty(it, file)?.addTo(properties).end() }
				}
				propValue.isArray -> {
					values = SmartList()
					propValue.processChildrenOfType<CwtValue> { resolveValue(it, file).addTo(values).end() }
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
		val config = CwtPropertyConfig(
			pointer, key, property.propertyValue,
			booleanValue, intValue, floatValue, stringValue, values, properties,
			documentation, options, optionValues, separatorType
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
		var documentationLines: LinkedList<String>? = null
		var options: LinkedList<CwtOptionConfig>? = null
		var optionValues: LinkedList<CwtOptionValueConfig>? = null
		
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
					properties = SmartList()
					value.processChildrenOfType<CwtProperty> { resolveProperty(it, file)?.addTo(properties).end() }
				}
				value.isArray -> {
					values = SmartList()
					value.processChildrenOfType<CwtValue> { resolveValue(it, file).addTo(values).end() }
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
						if(options == null) {
							options = LinkedList()
						} else {
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
		
		return CwtValueConfig(
			pointer, value.value,
			booleanValue, intValue, floatValue, stringValue,
			values, properties, documentation, options, optionValues
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
					options = SmartList()
					optionValue.processChildrenOfType<CwtOption> { resolveOption(it, file)?.addTo(options).end() }
				}
				optionValue.isArray -> {
					values = SmartList()
					optionValue.processChildrenOfType<CwtValue> { resolveOptionValue(it, file).addTo(values).end() }
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
						options = SmartList()
						option.processChildrenOfType<CwtOption> { resolveOption(it, file)?.addTo(options).end() }
					}
					option.isArray -> {
						values = SmartList()
						option.processChildrenOfType<CwtValue> { resolveOptionValue(it, file).addTo(values).end() }
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