package icu.windea.pls.images.dds.info

/**
 * 描述 DDS 文件头。
 *
 * 参见：[DDS_HEADER 结构 (Dds.h) - Win32 apps | Microsoft Learn](https://learn.microsoft.com/windows/win32/direct3ddds/dds-header)
 */
data class DdsHeader(
    val dwSize: Int,
    val dwFlags: Int,
    val dwHeight: Int,
    val dwWidth: Int,
    val dwPitchOrLinearSize: Int,
    val dwDepth: Int,
    val dwMipMapCount: Int,
    val dwReserved1: List<Int>, // size: 11
    val ddspf: DdsPixelFormat,
    val dwCaps: Int,
    val dwCaps2: Int,
    val dwCaps3: Int,
    val dwCaps4: Int,
    val dwReserved2: Int,
)
