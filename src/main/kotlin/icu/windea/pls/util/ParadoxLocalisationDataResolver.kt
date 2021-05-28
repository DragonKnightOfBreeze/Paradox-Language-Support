package icu.windea.pls.util

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * Paradox本地化文件的数据解析器。
 *
 * 返回值类型：`Map<String,String>`
 */
object ParadoxLocalisationDataResolver {
	fun resolve(file: PsiFile):Map<String,String>{
		if(file !is ParadoxLocalisationFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxLocalisationFile')")
		val result = mutableMapOf<String,String>()
		file.forEachChild {
			when(it){
				//如果是property，则仅提取key和value放入
				is ParadoxLocalisationProperty -> {
					if(it.isValid) {
						val name = it.name
						val value = it.value
						if(name.isNotEmpty() && value != null) result[name] = value
					}
				}
			}
		}
		return result
	}
}

