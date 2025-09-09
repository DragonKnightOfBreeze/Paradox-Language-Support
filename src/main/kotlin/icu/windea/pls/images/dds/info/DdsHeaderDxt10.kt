package icu.windea.pls.images.dds.info

/**
 * 用于处理资源数组的 DDS 标头扩展、不映射到旧版 Microsoft DirectDraw 像素格式结构的 DXGI 像素格式以及其他元数据。
 *
 * 如果 DDS_PIXELFORMAT 结构的 dwFourCC 成员设置为 `DX10`，则存在此标头。
 *
 * 参见：[DDS_HEADER_DXT10 结构 (Dds.h) - Win32 apps | Microsoft Learn](https://learn.microsoft.com/windows/win32/direct3ddds/dds-header-dxt10)
 */
data class DdsHeaderDxt10(
    val dxgiFormat: DxgiFormat,
    val resourceDimension: D3d10ResourceDimension,
    val miscFlag: Int,
    val arraySize: Int,
    val miscFlags2: Int,
)
