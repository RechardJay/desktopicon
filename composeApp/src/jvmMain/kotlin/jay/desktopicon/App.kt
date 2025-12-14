package jay.desktopicon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

typealias IconMap = Map<String, List<String>>
fun fetchAllIconsGrouped(): IconMap {
    val fileFinder = FileFinder()

    val groupedIcons = mutableMapOf<String, List<String>>()

    iconDirectory.forEach { folder ->
        val fileList = fileFinder.openFolder(folder)
        val iconList = fileFinder.filterIcon(fileList)
        // 将结果以 Map 的形式存储
        groupedIcons[folder] = iconList
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
        // 使用 LazyColumn 保证整体界面的垂直滚动性
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 遍历 Map 中的每个目录和其对应的图标列表
            items(iconMap.entries.toList()) { (folderPath, iconList) ->
                DirectorySection(folderPath, iconList)
            }
        }

        // 如果数据为空，显示加载状态
        if (iconMap.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

}
@Composable
fun DirectorySection(folderPath: String, data: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 目录标题
            Text(
                text = "目录: $folderPath (${data.size} 个图标)",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider()

            // 静态网格展示（限制行数，避免滚动）
            StaticHorizontalGrid(data = data, columns = 3)
        }
    }
}


@Composable
fun StaticHorizontalGrid(data: List<String>, columns: Int) {
    // 将一维数据列表分割成二维的行/列结构
    val rows = data.chunked(columns)

    // 使用 Column 包含所有的行
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { rowItems ->
            // 使用 Row 包含一行中的所有元素
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    // 使用 Modifier.weight(1f) 确保元素在 Row 中平均分配宽度
                    GridItem(text = item, modifier = Modifier.weight(1f))
                }
                // 填充最后一行的空白，确保网格对齐
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
@Composable
fun GridItem(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(60.dp).padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // 实际的图标渲染 (Text 仅为占位符)
        val name =  text.split('/').last()
        Text(text = name, maxLines = 1)
    }
}