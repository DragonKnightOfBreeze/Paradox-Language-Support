package icu.windea.pls.lang.expression

import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*

/**
 * 与当前脚本文件同一目录下的指定文件名的文件。（不允许在子目录下）
 * @see CwtDataType.FileName
 */
class ParadoxFileNamePathReferenceExpression : ParadoxPathReferenceExpression {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataType.FileName
    }
    
    override fun matchEntire(queryParameters: ParadoxFilePathSearch.SearchParameters): Boolean {
        return false
    }
    
    override fun matches(queryParameters: ParadoxFilePathSearch.SearchParameters, filePath: String, ignoreCase: Boolean): Boolean {
        //expression为空 - filePath需要是文件名且该文件需要直接位于contextFile所在目录下 
        //expression不为空 - filePath需要是文件名且该文件需要位于expression指定的目录（或其子目录）下
        val expression = queryParameters.configExpression?.value
        if(expression == null) {
            val contextParentPath = queryParameters.selector.fileInfo?.path?.parent ?: return false //contextParent is required
            val parentPath = filePath.substringBeforeLast('/')
            return contextParentPath == parentPath
        }
        return expression.matchesPath(filePath, ignoreCase)
    }
    
    override fun extract(configExpression: CwtDataExpression, filePath: String, ignoreCase: Boolean): String {
        return filePath.substringAfterLast('/')
    }
    
    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): String? {
        return null //信息不足
    }
    
    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): String {
        return pathReference.substringAfterLast('/') + ".dds"
    }
    
    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.general.unresolvedPathReference.description.fileName", pathReference, configExpression)
    }
}