@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.core.handler

import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理复杂枚举信息。
 */
object ParadoxComplexEnumInfoHandler {
	fun resolve(element: ParadoxScriptExpressionElement, parentStub: StubElement<*>? = null): ParadoxComplexEnumInfo? {
		if(element.isParameterAwareExpression()) return null //快速判断
		
		val file = element.containingFile
		val project = file.project
		val fileInfo = file.fileInfo ?: return null
		val path = fileInfo.path
		val gameType = fileInfo.rootInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val isKey = element is ParadoxScriptPropertyKey
		val lazyElementPath = lazy { ParadoxElementPathHandler.resolveFromFile(element) }
		val complexEnumConfig = configGroup.getComplexEnumConfig(path, lazyElementPath, isKey)  ?: return null
		val name = element.value
		val enumName = complexEnumConfig.name
		return ParadoxComplexEnumInfo(name, enumName)
	}
	
	fun matchesComplexEnumByPath(complexEnum: CwtComplexEnumConfig, path: ParadoxPath): Boolean {
		return complexEnum.path.any {
			(complexEnum.pathFile == null || complexEnum.pathFile == path.fileName)
				&& (if(complexEnum.pathStrict) it == path.parent else it.matchesPath(path.parent))
		}
	}
	
	fun matchesComplexEnumByElementPath(complexEnum: CwtComplexEnumConfig, lazyElementPath: Lazy<ParadoxElementPath?>, isKey: Boolean) : Boolean{
		
		return true
	}
}