package icu.windea.pls.lang.expression

import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*

/**
 * @see CwtDataType.FileName
 */
class ParadoxFileNamePathReferenceExpression : ParadoxPathReferenceExpression {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataType.FileName
    }
    
    override fun matches(configExpression: CwtDataExpression, filePath: String, ignoreCase: Boolean): Boolean {
        return true
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
        return PlsBundle.message("inspection.script.general.unresolvedFilePath.description.fileName", pathReference)
    }
}