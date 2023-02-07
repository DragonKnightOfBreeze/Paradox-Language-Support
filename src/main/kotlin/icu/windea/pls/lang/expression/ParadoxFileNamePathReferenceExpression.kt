package icu.windea.pls.lang.expression

import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*

/**
 * 与当前脚本文件同一目录下的指定文件名的文件。（不允许在子目录下）
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
        return PlsBundle.message("inspection.script.general.unresolvedPathReference.description.fileName", pathReference, configExpression)
    }
}