package icu.windea.pls.ep.tools.exporter

import com.intellij.icons.AllIcons
import icu.windea.pls.core.data.JsonService
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import kotlin.io.path.outputStream

/**
 * 使用 JSON 文件作为数据文件的模组导出器。
 */
abstract class ParadoxJsonBasedModExporter : ParadoxModExporter {
    override val icon get() = AllIcons.FileTypes.Json

    override fun isAvailable(gameType: ParadoxGameType) = true

    protected suspend fun <T> writeData(filePath: Path, data: T, serializer: KSerializer<T>) {
        withContext(Dispatchers.IO) {
            // 这里不需要使用 edtWriteAction
            doWriteData(filePath, data, serializer)
        }
    }

    private fun <T> doWriteData(filePath: Path, data: T, serializer: KSerializer<T>) {
        try {
            filePath.outputStream().use { JsonService.json.encodeToStream(serializer, data, it) }
        } catch (e: Exception) {
            throw IllegalStateException(ChronicleEpBundle.message("mod.exporter.error.data", filePath), e)
        }
    }
}
