package icu.windea.pls.ep.tools.exporter

import com.intellij.icons.AllIcons
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.data.JsonService
import icu.windea.pls.ep.PlsEpBundle
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * 使用 JSON 文件作为数据文件的模组导出器。
 */
abstract class ParadoxJsonBasedModExporter : ParadoxModExporter {
    override val icon get() = AllIcons.FileTypes.Json

    override fun isAvailable(gameType: ParadoxGameType) = true

    protected suspend fun writeData(filePath: Path, data: Any) {
        withContext(Dispatchers.IO) {
            // 这里不需要使用 edtWriteAction
            doWriteData(filePath, data)
        }
    }

    private fun doWriteData(filePath: Path, data: Any) {
        try {
            JsonService.jsonMapper.writeValue(filePath.toFile(), data)
        } catch (e: Exception) {
            throw IllegalStateException(PlsEpBundle.message("mod.exporter.error.data", filePath), e)
        }
    }
}
