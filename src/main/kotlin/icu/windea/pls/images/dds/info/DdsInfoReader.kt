package icu.windea.pls.images.dds.info

import java.io.EOFException
import java.io.InputStream

/**
 * 用于从 DDS 文件头中读取 DDS 文件信息。
 *
 * 参见：[DDS 编程指南 - Win32 apps | Microsoft Learn](https://learn.microsoft.com/windows/win32/direct3ddds/dx-graphics-dds-pguide)
 *
 * @see DdsInfo
 */
object DdsInfoReader {
    @Throws(EOFException::class, IllegalArgumentException::class)
    fun read(input: InputStream): DdsInfo {
        return readDdsInfo(input)
    }

    private fun readDdsInfo(input: InputStream): DdsInfo {
        val dwMagic = input.readDword().takeIf { it == DdsConstants.DDS_MAGIC } ?: throw IllegalArgumentException("Invalid dds magic")
        val header = readDdsHeader(input)
        val header10 = if (header.ddspf.shouldLoadHeader10()) readDdsHeaderDxt10(input) else null
        return DdsInfo(
            dwMagic = dwMagic,
            header = header,
            header10 = header10,
        )
    }

    private fun readDdsHeader(input: InputStream): DdsHeader {
        return DdsHeader(
            dwSize = input.readDword().takeIf { it == DdsConstants.HEADER_SIZE } ?: throw IllegalArgumentException("Invalid dds header size"),
            dwFlags = input.readDword(),
            dwHeight = input.readDword(),
            dwWidth = input.readDword(),
            dwPitchOrLinearSize = input.readDword(),
            dwDepth = input.readDword(),
            dwMipMapCount = input.readDword(),
            dwReserved1 = MutableList(11) { input.readDword() },
            ddspf = readDdsPixelFormat(input),
            dwCaps = input.readDword(),
            dwCaps2 = input.readDword(),
            dwCaps3 = input.readDword(),
            dwCaps4 = input.readDword(),
            dwReserved2 = input.readDword(),
        )
    }

    private fun readDdsPixelFormat(input: InputStream): DdsPixelFormat {
        return DdsPixelFormat(
            dwSize = input.readDword().takeIf { it == DdsConstants.PIXEL_FORMAT_SIZE } ?: throw IllegalArgumentException("Invalid dds pixel format size"),
            dwFlags = input.readDword(),
            dwFourCC = input.readDword(),
            dwRGBBitCount = input.readDword(),
            dwRBitMask = input.readDword(),
            dwGBitMask = input.readDword(),
            dwBBitMask = input.readDword(),
            dwABitMask = input.readDword()
        )
    }

    private fun readDdsHeaderDxt10(input: InputStream): DdsHeaderDxt10 {
        return DdsHeaderDxt10(
            dxgiFormat = DxgiFormat.get(input.readUInt()),
            resourceDimension = D3d10ResourceDimension.get(input.readUInt()),
            miscFlag = input.readUInt(),
            arraySize = input.readUInt().takeIf { it != 0 } ?: throw IllegalArgumentException("Invalid dds header dxt10 array size"),
            miscFlags2 = input.readUInt(),
        )
    }

    private fun InputStream.readByte(): Int {
        return read().also { if (it == -1) throw EOFException() }
    }

    private fun InputStream.readUInt(): Int {
        val b1 = readByte() and 0xff
        val b2 = readByte() and 0xff
        val b3 = readByte() and 0xff
        val b4 = readByte() and 0xff
        return b1 or (b2 shl 8) or (b3 shl 16) or (b4 shl 24)
    }

    private fun InputStream.readDword(): Int {
        return readUInt()
    }
}
