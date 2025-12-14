package jay.desktopicon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File
import kotlin.text.Charsets

// 数据类，用于存储解析后的 .desktop 文件信息
data class IconInfo(
    val name: String,
    val icon: String, // 这可以是图标名称或完整路径
    val filePath: String
)

typealias IconMap = Map<String, List<IconInfo>>

// 解析 .desktop 文件
fun parseDesktopFile(filePath: String): IconInfo? {
    val properties = mutableMapOf<String, String>()
    var inDesktopEntry = false
    try {
        File(filePath).forEachLine(Charsets.UTF_8) { line ->
            val trimmedLine = line.trim()
            if (trimmedLine == "[Desktop Entry]") {
                inDesktopEntry = true
            } else if (inDesktopEntry && trimmedLine.isNotBlank() && !trimmedLine.startsWith("#")) {
                if (trimmedLine.startsWith("[")) {
                    inDesktopEntry = false // 遇到新的 section，停止解析
                } else {
                    val parts = trimmedLine.split("=", limit = 2)
                    if (parts.size == 2) {
                        properties[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
        }

        val name = properties["Name"]
        val icon = properties["Icon"]

        return if (name != null && icon != null) {
            IconInfo(name, icon, filePath)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}


fun fetchAllIconsGrouped(): IconMap {
    val fileFinder = FileFinder()
    val groupedIcons = mutableMapOf<String, List<IconInfo>>()

    iconDirectory.forEach { folder ->
        val fileList = fileFinder.openFolder(folder)
        val desktopFiles = fileFinder.filterIcon(fileList)
        // 解析每个 .desktop 文件并过滤掉解析失败的 null
        val iconList = desktopFiles.mapNotNull { parseDesktopFile(it) }
        if (iconList.isNotEmpty()) {
            groupedIcons[folder] = iconList
        }
    }
    return groupedIcons
}

@Composable
@Preview
fun App() {
    val iconMap by produceState<IconMap>(initialValue = emptyMap()) {
        value = withContext(Dispatchers.IO) {
            fetchAllIconsGrouped()
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("桌面图标查看器") }) }
    ) { padding ->
        if (iconMap.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(iconMap.entries.toList()) { (folderPath, iconList) ->
                    DirectorySection(folderPath, iconList)
                }
            }
        }
    }
}

@Composable
fun DirectorySection(folderPath: String, data: List<IconInfo>) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "$folderPath (${data.size} 个)",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        StaticHorizontalGrid(data = data, columns = 5) // 增加列数以更好地适应图标
    }
}

@Composable
fun StaticHorizontalGrid(data: List<IconInfo>, columns: Int) {
    val rows = data.chunked(columns)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        IconCard(item)
                    }
                }
                // 填充空位
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun IconCard(info: IconInfo) {
    val iconPainter = rememberIconPainter(info.icon)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp).height(100.dp) // 固定高度
        ) {
            Image(
                painter = iconPainter,
                contentDescription = info.name,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = info.name,
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
