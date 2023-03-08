package icu.windea.pls.lang.expression

import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*

/**
 * @see CwtDataType.Icon
 */
class ParadoxIconReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport() {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataType.Icon
    }
    
    override fun matchEntire(queryParameters: ParadoxFilePathSearch.SearchParameters): Boolean {
        return false
    }
    
    override fun matches(queryParameters: ParadoxFilePathSearch.SearchParameters, filePath: String, ignoreCase: Boolean): Boolean {
        val expression = queryParameters.configExpression?.value ?: return false
        return expression.matchesPath(filePath, ignoreCase) && filePath.endsWith(".dds", true)
    }
    
    override fun extract(configExpression: CwtDataExpression, filePath: String, ignoreCase: Boolean): String? {
        val expression = configExpression.value ?: return null
        return filePath.removeSurroundingOrNull(expression, ".dds", ignoreCase)?.substringAfterLast('/')
    }
    
    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): String? {
        return null //信息不足
    }
    
    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): String {
        return pathReference.substringAfterLast('/')
    }
    
    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.general.unresolvedPathReference.description.icon", pathReference, configExpression)
    }
}

/**
 * @see CwtDataType.FilePath
 */
class ParadoxFilePathReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport() {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataType.FilePath
    }
    
    override fun matchEntire(queryParameters: ParadoxFilePathSearch.SearchParameters): Boolean {
        return queryParameters.configExpression?.value == null
    }
    
    override fun matches(queryParameters: ParadoxFilePathSearch.SearchParameters, filePath: String, ignoreCase: Boolean): Boolean {
        val expression = queryParameters.configExpression?.value ?: return true
        val index = expression.lastIndexOf(',') //","应当最多出现一次
        if(index == -1) {
            //匹配父路径
            return expression.matchesPath(filePath, ignoreCase)
        } else {
            //匹配父路径+文件名前缀+扩展名
            val parentAndFileNamePrefix = expression.substring(0, index)
            if(!filePath.startsWith(parentAndFileNamePrefix, ignoreCase)) return false
            val fileNameSuffix = expression.substring(index + 1)
            return filePath.endsWith(fileNameSuffix, ignoreCase)
        }
    }
    
    override fun extract(configExpression: CwtDataExpression, filePath: String, ignoreCase: Boolean): String? {
        val expression = configExpression.value ?: return filePath
        val index = expression.lastIndexOf(',') //","应当最多出现一次
        if(index == -1) {
            return filePath.removePrefixOrNull(expression, ignoreCase)?.trimStart('/')
        } else {
            val s1 = expression.substring(0, index)
            val s2 = expression.substring(index + 1)
            return filePath.removeSurroundingOrNull(s1, s2, ignoreCase)?.trimStart('/')
        }
    }
    
    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): String {
        val expression = configExpression.value ?: pathReference
        val index = configExpression.lastIndexOf(',') //","应当最多出现一次
        if(index == -1) {
            if(expression.endsWith('/')) {
                return expression + pathReference
            } else {
                return "$expression/$pathReference"
            }
        } else {
            return expression.replace(",", pathReference)
        }
    }
    
    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): String {
        val expression = configExpression.value ?: pathReference.substringAfterLast('/')
        val index = expression.lastIndexOf(',') //","应当最多出现一次
        if(index == -1) {
            return pathReference.substringAfterLast('/')
        } else {
            return expression.replace(",", pathReference).substringAfterLast('/')
        }
    }
    
    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.general.unresolvedPathReference.description.filePath", pathReference, configExpression)
    }
}

/**
 * 与当前脚本文件同一目录下的指定文件名的文件。（不允许在子目录下）
 * @see CwtDataType.FileName
 */
class ParadoxFileNameReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport() {
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