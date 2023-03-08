package icu.windea.pls.lang.expression

import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*

/**
 * @see CwtDataType.Icon
 */
class ParadoxIconPathReferenceExpression : ParadoxPathReferenceExpression {
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