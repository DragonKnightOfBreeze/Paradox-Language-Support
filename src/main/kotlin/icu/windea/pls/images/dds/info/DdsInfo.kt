package icu.windea.pls.images.dds.info

/**
 * DDS 文件信息。
 *
 * 参见：[DDS 编程指南 - Win32 apps | Microsoft Learn](https://learn.microsoft.com/windows/win32/direct3ddds/dx-graphics-dds-pguide)
 *
 * @property width 宽度。来自 `DDS_HEADER.dwWidth`。
 * @property height 高度。来自 `DDS_HEADER.dwHeight`。
 * @property d3dFormat D3D 格式。示例：`DXT5`。
 * @property dxgiFormat DXGI 格式。示例：`BC3_UNORM`。
 * @property format 资源格式。示例：`DXGI_FORMAT_R8G8B8A8_UNORM`、`D3DFMT_A8B8G8R8`。
 *
 * @see DdsInfoReader
 */
data class DdsInfo(
    val dwMagic: Int,
    val header: DdsHeader,
    val header10: DdsHeaderDxt10?,
) {
    val width: Int = header.dwWidth
    val height: Int = header.dwHeight
    val d3dFormat: String = header.ddspf.d3dFormat().toString()
    val dxgiFormat: String? = header10?.dxgiFormat?.toString()
    val format: String = dxgiFormat?.let { "DXGI_FORMAT_$it" } ?: "D3DFMT_$d3dFormat"
}
