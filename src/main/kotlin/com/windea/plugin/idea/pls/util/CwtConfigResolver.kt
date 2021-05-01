package com.windea.plugin.idea.pls.util

import com.intellij.psi.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.config.*
import com.windea.plugin.idea.pls.cwt.psi.*
import java.util.*

/**
 * Cwt配置文件的数据解析器。
 *
 * 返回值类型：[CwtConfig]
 */
object CwtConfigResolver {
	fun resolve(file: PsiFile): CwtConfig {
		if(file !is CwtFile) throw java.lang.IllegalArgumentException("Invalid file type")
		val rootBlock = file.rootBlock ?: return EmptyCwtConfig
		return resolveRootBlock(rootBlock)
	}
	
	fun resolveRootBlock(rootBlock: CwtRootBlock): CwtConfig {
		return when {
			rootBlock.isEmpty -> EmptyCwtConfig
			rootBlock.isArray -> CwtConfig(rootBlock.valueList.mapNotNull { resolveValue(it) }, emptyList())
			rootBlock.isObject -> CwtConfig(emptyList(), rootBlock.propertyList.mapNotNull { resolveProperty(it) })
			else -> EmptyCwtConfig
		}
	}
	
	private fun resolveProperty(property: CwtProperty): CwtConfigProperty? {
		val key = property.propertyName
		val (documentation, options) = getDocumentationAndOptions(property)
		var value: String? = null
		var values: List<CwtConfigValue>? = null
		var properties: List<CwtConfigProperty>? = null
		val propertyValue = property.value
		when {
			propertyValue == null -> return null
			propertyValue is CwtString -> value = propertyValue.value
			propertyValue is CwtBlock -> {
				when {
					propertyValue.isEmpty -> {
						values = emptyList()
						properties = emptyList()
					}
					propertyValue.isArray -> {
						values = propertyValue.valueList.map { resolveValue(it) }
						properties = emptyList()
					}
					propertyValue.isObject -> {
						values = emptyList()
						properties = propertyValue.propertyList.mapNotNull { resolveProperty(it) }
					}
				}
			}
		}
		return CwtConfigProperty(key, value, values, properties, options, documentation)
	}
	
	private fun resolveValue(value: CwtValue): CwtConfigValue {
		val (documentation, options) = getDocumentationAndOptions(value)
		var _value: String? = null
		var values: List<CwtConfigValue>? = null
		var properties: List<CwtConfigProperty>? = null
		when {
			value is CwtString -> _value = value.value
			value is CwtBlock -> {
				when {
					value.isEmpty -> {
						values = emptyList()
						properties = emptyList()
					}
					value.isArray -> {
						values = value.valueList.map { resolveValue(it) }
						properties = emptyList()
					}
					value.isObject -> {
						values = emptyList()
						properties = value.propertyList.mapNotNull { resolveProperty(it) }
					}
				}
			}
		}
		return CwtConfigValue(_value, values, properties, documentation, options)
	}
	
	private fun getDocumentationAndOptions(element: PsiElement): Pair<String?, CwtConfigOptions?> {
		var current = element
		val documentationComments = LinkedList<CwtDocumentationComment>()
		val optionComments = LinkedList<CwtOptionComment>()
		while(true) {
			current = current.prevSibling?:break
			when {
				current is PsiWhiteSpace || current is PsiComment -> continue
				current is CwtDocumentationComment -> documentationComments.addFirst(current)
				current is CwtOptionComment -> optionComments.addFirst(current)
				else -> break
			}
		}
		val documentation = getDocumentation(documentationComments)
		val options = getOptions(optionComments)
		return documentation to options
	}
	
	private fun getDocumentation(documentationComments: MutableList<CwtDocumentationComment>): String? {
		if(documentationComments.isEmpty()) return null
		return documentationComments.joinToString("\n") { it.documentationText?.text.orEmpty() }.trim()
	}
	
	private fun getOptions(optionComments: MutableList<CwtOptionComment>): CwtConfigOptions? {
		if(optionComments.isEmpty()) return null
		var cardinality: String? = null
		var optional: Boolean? = null
		var required: Boolean? = null
		var type_key_filter: ReversibleList<String>? = null
		var severity: String? = null
		var push_scope: String? = null
		var replace_scope: Map<String, String>? = null
		var scopes: List<String>? = null
		var graph_related_types: List<String>? = null
		for(optionComment in optionComments) {
			for(value in optionComment.valueList) {
				when(value.value) {
					"optional" -> optional = true
					"required" -> required = true
				}
			}
			for(option in optionComment.optionList) {
				val optionName = option.name
				val optionValue = option.value ?: continue
				when(optionName) {
					"cardinality" -> {
						if(optionValue is CwtString) cardinality = optionValue.value
					}
					"type_key_filter" -> {
						val optionSeparator = option.optionSeparator?.text ?: continue
						val reverse = optionSeparator == "<>"
						when {
							optionValue is CwtString -> type_key_filter = ReversibleList(listOf(optionValue.value),reverse)
							optionValue is CwtBlock -> {
								val list = mutableListOf<String>()
								for(v in optionValue.valueList) {
									if(v is CwtString) list.add(v.value)
								}
								type_key_filter = ReversibleList(list,reverse)
							}
						}
					}
					"severity" -> {
						if(optionValue is CwtString) severity = optionValue.value
					}
					"push_scope" -> {
						if(optionValue is CwtString) push_scope = optionValue.value
					}
					"replace_scope" -> {
						if(optionValue is CwtBlock) {
							val map = mutableMapOf<String, String>()
							for(o in optionValue.optionList) {
								val n = o.optionName
								val v = o.value
								if(v is CwtString) map.put(n, v.value)
							}
							replace_scope = map
						}
					}
					"scope" -> {
						when {
							optionValue is CwtString -> scopes = listOf(optionValue.value)
							optionValue is CwtBlock -> {
								val list = mutableListOf<String>()
								for(v in optionValue.valueList) {
									if(v is CwtString) list.add(v.value)
								}
								scopes = list
							}
						}
					}
					"graph_related_types" -> {
						if(optionValue is CwtBlock) {
							val list = mutableListOf<String>()
							for(v in optionValue.valueList) {
								if(v is CwtString) list.add(v.value)
							}
							graph_related_types = list
						}
					}
				}
			}
		}
		return CwtConfigOptions(cardinality, optional, required, type_key_filter, severity, push_scope, replace_scope, scopes, graph_related_types)
	}
}