package jay.desktopicon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import java.io.File

// 常见的图标目录
val iconPaths = listOf(
    "/usr/share/icons/hicolor/scalable/apps/",
    "/usr/share/icons/hicolor/48x48/apps/",
    "/usr/share/pixmaps/",
    "/usr/share/icons/"
)

// 支持的图标文件扩展名 - 移除 .xpm，因为它不被直接支持
val iconExtensions = listOf(".svg", ".png")

/**
 * 一个 Composable 函数，用于记住并加载图标。
 * 它会根据提供的图标名称或路径，在标准目录中查找并加载。
 *
 * @param iconNameOrPath .desktop 文件中的 Icon 字段值。
 * @return 返回一个 Painter 对象用于渲染。如果找不到或加载失败，则返回一个白色色块。
 */
@Composable
fun rememberIconPainter(iconNameOrPath: String): Painter {
    // 记住计算出的图标文件，避免在每次重组时都重新计算
    val iconFile = remember(iconNameOrPath) { findIconFile(iconNameOrPath) }

    // 记住白色的 ColorPainter，以避免在每次重组时都创建新实例
    val fallbackPainter = remember { ColorPainter(Color.White) }

    return if (iconFile != null) {
        // 根据文件扩展名选择不同的加载方式
        when (iconFile.extension.lowercase()) {
            "svg" -> {
                // 记住从 SVG 文件加载的 Painter
                remember(iconFile.absolutePath) {
                    loadSvgPainter(iconFile.inputStream(), Density(1f))
                }
            }

            "png" -> {
                // 记住从 PNG 文件加载的位图
                val bitmap: ImageBitmap = remember(iconFile.absolutePath) {
                    loadImageBitmap(iconFile.inputStream())
                }
                BitmapPainter(bitmap)
            }

            else -> {
                // 对于其他（当前不支持的）类型，返回白色色块
                fallbackPainter
            }
        }
    } else {
        // 如果找不到文件，直接返回白色色块
        fallbackPainter
    }
}

/**
 * 根据图标名称或路径查找图标文件。
 *
 * 1. 如果提供的是绝对路径，直接检查文件是否存在。
 * 2. 如果是图标名称，则在预定义的 `iconPaths` 目录中搜索。
 *
 * @param iconNameOrPath 图标名称或绝对路径。
 * @return 如果找到，返回 File 对象，否则返回 null。
 */
fun findIconFile(iconNameOrPath: String): File? {
    // 1. 检查是否为绝对路径
    val asFile = File(iconNameOrPath)
    if (asFile.isAbsolute && asFile.exists()) {
        return asFile
    }

    // 2. 在标准图标目录中搜索
    for (path in iconPaths) {
        for (ext in iconExtensions) {
            val file = File("$path$iconNameOrPath$ext")
            if (file.exists()) {
                return file
            }
        }
    }

    // 3. 作为最后的手段，尝试在 hicolor 主题的各个尺寸目录中查找
    val hicolorBase = "/usr/share/icons/hicolor/"
    File(hicolorBase).listFiles { dir, _ -> dir.isDirectory }?.forEach { sizeDir ->
        val appDir = File(sizeDir, "apps")
        if (appDir.exists()) {
            for (ext in iconExtensions) {
                val file = File(appDir, "$iconNameOrPath$ext")
                if (file.exists()) {
                    return file
                }
            }
        }
    }

    return null // 未找到
}
