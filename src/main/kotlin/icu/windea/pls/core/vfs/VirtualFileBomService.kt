package icu.windea.pls.core.vfs

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ArrayUtil
import java.io.IOException

object VirtualFileBomService {
    val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())

    /**
     * 判断当前内虚拟文件是否包含指定 BOM（物理层面）。
     */
    fun hasBom(file: VirtualFile, bom: ByteArray?): Boolean {
        val actualBom = file.bom
        return if (bom == null) actualBom == null else bom contentEquals actualBom
    }

    /**
     * 添加 BOM 到当前虚拟文件（物理层面）。
     */
    @Throws(IOException::class)
    fun addBom(file: VirtualFile, bom: ByteArray, wait: Boolean = true) {
        file.bom = bom
        val bytes = file.contentsToByteArray()
        val contentWithAddedBom = ArrayUtil.mergeArrays(bom, bytes)
        if (wait) {
            WriteAction.runAndWait<IOException> { file.setBinaryContent(contentWithAddedBom) }
        } else {
            WriteAction.run<IOException> { file.setBinaryContent(contentWithAddedBom) }
        }
    }

    /**
     * 从当前虚拟文件移除 BOM（物理层面）。
     */
    @Throws(IOException::class)
    fun removeBom(file: VirtualFile, bom: ByteArray, wait: Boolean = true) {
        file.bom = null
        val bytes = file.contentsToByteArray()
        val contentWithStrippedBom = bytes.copyOfRange(bom.size, bytes.size)
        if (wait) {
            WriteAction.runAndWait<IOException> { file.setBinaryContent(contentWithStrippedBom) }
        } else {
            WriteAction.run<IOException> { file.setBinaryContent(contentWithStrippedBom) }
        }
    }
}
