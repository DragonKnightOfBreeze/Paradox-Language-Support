package icu.windea.pls.config.cwt

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
		if(rootBlock.isEmpty) return CwtFileConfig.EmptyConfig
		val properties = SmartList<CwtPropertyConfig>()
		val values = SmartList<CwtValueConfig>()
		val fileConfig = CwtFileConfig(file.createPointer(), properties, values)
		rootBlock.processChild {
			when {
				it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(properties).end()
				it is CwtValue -> resolveValue(it, file, fileConfig).addTo(values).end()
				else -> end()
			}
		}
		return fileConfig
	}
	
	private fun resolveProperty(property: CwtProperty, file: CwtFile, fileConfig: CwtFileConfig): CwtPropertyConfig? {
		val pointer = property.createPointer(file)
		val key = property.propertyName
		val propValue = property.value ?: return null
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var properties: List<CwtPropertyConfig>? = null
		var values: List<CwtValueConfig>? = null
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
					properties = emptyList()
					values = emptyList()
				}
				else -> {
					properties = SmartList()
					values = SmartList()
					propValue.processChild {
						when {
							it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(properties).end()
							it is CwtValue -> resolveValue(it, file, fileConfig).addTo(values).end()
							else -> end()
						}
					}
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
						val resolvedOption = resolveOption(option, file, fileConfig)
						if(resolvedOption != null) options.addFirst(resolvedOption)
					} else {
						val optionValue = current.value
						if(optionValue != null) {
							if(optionValues == null) optionValues = LinkedList()
							val resolvedOptionValue = resolveOptionValue(optionValue, file, fileConfig)
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
			pointer, fileConfig.info, key, property.propertyValue,
			booleanValue, intValue, floatValue, stringValue, properties, values,
			documentation, options, optionValues, separatorType
		)
		properties?.forEach { it.parent = config }
		values?.forEach { it.parent = config }
		return config
	}
	
	private fun resolveValue(value: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtValueConfig {
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
				else -> {
					properties = SmartList()
					values = SmartList()
					value.processChild {
						when {
							it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(properties).end()
							it is CwtValue -> resolveValue(it, file, fileConfig).addTo(values).end()
							else -> end()
						}
					}
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
						if(options == null) options = LinkedList()
						val resolvedOption = resolveOption(option, file, fileConfig)
						if(resolvedOption != null) options.addFirst(resolvedOption)
					} else {
						val optionValue = current.value
						if(optionValue != null) {
							if(optionValues == null) optionValues = LinkedList()
							val resolvedOptionValue = resolveOptionValue(optionValue, file, fileConfig)
							optionValues.addFirst(resolvedOptionValue)
						}
					}
				}
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		val documentation = documentationLines?.joinToString("\n")
		
		val config = CwtValueConfig(
			pointer, fileConfig.info, value.value,
			booleanValue, intValue, floatValue, stringValue,
			properties, values, documentation, options, optionValues
		)
		properties?.forEach { it.parent = config }
		values?.forEach { it.parent = config }
		return config
	}
	
	private fun resolveOption(option: CwtOption, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionConfig? {
		val key = option.optionName
		val optionValue = option.value ?: return null
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var options: List<CwtOptionConfig>? = null
		var optionValues: List<CwtOptionValueConfig>? = null
		val separatorType = option.separatorType
		when {
			optionValue is CwtBoolean -> booleanValue = optionValue.booleanValue
			optionValue is CwtInt -> intValue = optionValue.intValue
			optionValue is CwtFloat -> floatValue = optionValue.floatValue
			optionValue is CwtString -> stringValue = optionValue.stringValue
			optionValue is CwtBlock -> when {
				optionValue.isEmpty -> {
					options = emptyList()
					optionValues = emptyList()
				}
				else -> {
					options = SmartList()
					optionValues = SmartList()
					optionValue.processChild {
						when {
							it is CwtOption -> resolveOption(it, file, fileConfig)?.addTo(options).end()
							it is CwtValue -> resolveOptionValue(it, file, fileConfig).addTo(optionValues).end()
							else -> end()
						}
					}
				}
			}
		}
		return CwtOptionConfig(
			emptyPointer(), fileConfig.info, key, optionValue.value,
			booleanValue, intValue, floatValue, stringValue, options, optionValues, separatorType
		)
	}
	
	private fun resolveOptionValue(option: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionValueConfig {
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var options: List<CwtOptionConfig>? = null
		var optionValues: List<CwtOptionValueConfig>? = null
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
						options = emptyList()
						optionValues = emptyList()
					}
					else -> {
						options = SmartList()
						optionValues = SmartList()
						option.processChild {
							when {
								it is CwtOption -> resolveOption(it, file, fileConfig)?.addTo(options).end()
								it is CwtValue -> resolveOptionValue(it, file, fileConfig).addTo(optionValues).end()
								else -> end()
							}
						}
					}
				}
			}
		}
		return CwtOptionValueConfig(
			emptyPointer(), fileConfig.info, option.value,
			booleanValue, intValue, floatValue, stringValue, options, optionValues
		)
	}
}