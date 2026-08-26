package icu.windea.pls.core

import it.unimi.dsi.fastutil.objects.Object2IntMap
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream

class IndexExtensionsTest {
    private fun roundTrip(write: (DataOutput) -> Unit, read: (DataInput) -> Unit) {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out -> write(out) }
        DataInputStream(ByteArrayInputStream(bytes.toByteArray())).use { input -> read(input) }
    }

    @Test
    fun writeByte_readByte_test() {
        roundTrip(
            write = { it.writeByte(5.toByte()) },
            read = { Assert.assertEquals(5.toByte(), it.readByte()) },
        )
        roundTrip(
            write = { it.writeByte((-1).toByte()) },
            read = { Assert.assertEquals((-1).toByte(), it.readByte()) },
        )
    }

    @Test
    fun writeIntFast_readIntFast_test() {
        val values = intArrayOf(0, 1, -1, 127, 128, 255, 256, 1024, -1024, Int.MAX_VALUE, Int.MIN_VALUE)
        for (v in values) {
            roundTrip(
                write = { it.writeIntFast(v) },
                read = { Assert.assertEquals(v, it.readIntFast()) },
            )
        }
    }

    @Test
    fun writeUTFFast_readUTFFast_test() {
        val values = listOf("", "abc", "hello world", "中文文本", "a\nb\tc", "emoji: \uD83D\uDE00")
        for (v in values) {
            roundTrip(
                write = { it.writeUTFFast(v) },
                read = { Assert.assertEquals(v, it.readUTFFast()) },
            )
        }
    }

    @Test
    fun writeOrReadFrom_roundTrip_test() {
        // from 为空：直接写值
        roundTrip(
            write = { it.writeOrWriteFrom("a", null, { s -> s }, { v -> it.writeUTF(v) }) },
            read = { Assert.assertEquals("a", it.readOrReadFrom(null, { s -> s }, { it.readUTF() })) },
        )
        // from 非空且 selector 相等：仅写 true，读取时复用 from
        val from = "abc"
        roundTrip(
            write = { it.writeOrWriteFrom("abc", from, { s -> s }, { v -> it.writeUTF(v) }) },
            read = { Assert.assertEquals("abc", it.readOrReadFrom(from, { s -> s }, { it.readUTF() })) },
        )
        // from 非空但 selector 不相等：写 false + 值
        roundTrip(
            write = { it.writeOrWriteFrom("abd", from, { s -> s }, { v -> it.writeUTF(v) }) },
            read = { Assert.assertEquals("abd", it.readOrReadFrom(from, { s -> s }, { it.readUTF() })) },
        )
    }

    @Test
    fun writeIndexedStringList_readIndexedStringList_test() {
        val list = listOf("a", "b", "a", "c", "b", "d")
        var keyToIndex: Object2IntMap<String>? = null
        roundTrip(
            write = { keyToIndex = it.writeIndexedStringList(list) { s -> s } },
            read = {
                val indexToKey = it.readIndexedStringList()
                Assert.assertEquals(keyToIndex!!.size, indexToKey.size)
                // 写入返回的 key->index 与读取的 index->key 应保持一致
                indexToKey.forEach { (idx, key) -> Assert.assertEquals(idx, keyToIndex.getInt(key)) }
            },
        )
    }

    @Test
    fun writeIndexedStringList_nullTransform_test() {
        val list = listOf("a", "b", "c")
        var keyToIndex: Object2IntMap<String>? = null
        roundTrip(
            write = { keyToIndex = it.writeIndexedStringList(list) { s -> if (s == "b") null else s } },
            read = {
                val indexToKey = it.readIndexedStringList()
                // null 项被忽略
                Assert.assertEquals(2, keyToIndex!!.size)
                Assert.assertEquals(2, indexToKey.size)
            },
        )
    }
}
