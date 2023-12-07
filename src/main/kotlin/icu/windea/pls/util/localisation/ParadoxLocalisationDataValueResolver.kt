package icu.windea.pls.util.localisation

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import kotlin.collections.set

@Suppress("unused")
object ParadoxLocalisationDataValueResolver {
	/**
	 * 解析本地化文件的数据。跳过不合法的[PsiElement]。如果有重复的[ParadoxLocalisationProperty]，使用最后一个。
	 */
	fun resolve(file: PsiFile): Map<String, Map<String, String>> {
		if(file !is ParadoxLocalisationFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxLocalisationFile')")
		val result: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
		file.processChildren file@{ fileItem ->
			if(!fileItem.isValid) return@file true
			when {
				fileItem is ParadoxLocalisationPropertyList -> {
					val locale = fileItem.findChild<ParadoxLocalisationLocale>() ?: return@file true
					val localeName = locale.name
					val properties = mutableMapOf<String, String>()
					result[localeName] = properties
					fileItem.processChildren list@{ listItem ->
						if(!listItem.isValid) return@list true
						when {
							listItem is ParadoxLocalisationProperty -> {
								resolveProperty(listItem, properties)
								true
							}
							else -> true
						}
					}
				}
				else -> true
			}
		}
		return result
	}
	
	fun resolveProperty(property: ParadoxLocalisationProperty, result: MutableMap<String, String>) {
		val name = property.name
		val value = property.value ?: return
		result[name] = value
	}
}

