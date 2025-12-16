package jay.desktopicon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import kotlin.random.Random

fun main() {
    val allIconsGrouped: IconMap = fetchAllIconsGrouped()
    val iconInfos = allIconsGrouped.values.toList()[0]
    val iconInfo = iconInfos[Random.nextInt(0, iconInfos.size)]
    val desktopFile: IconInfo? = parseDesktopFile(iconInfo.filePath)
    println(desktopFile)
}

@Composable
fun showDetailNewWindow(iconInfo: IconInfo, onClose: () -> Unit) {
    Window(
        onCloseRequest = onClose, // 关闭次级窗口只修改状态
        title = "详情 - ${iconInfo.name}"
    ) {
        val scope = rememberCoroutineScope()
        Column {
            Row (horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = { scope.launch { openSource(iconInfo.filePath) } }
                ) {
                    Text("打开源文件")
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(iconInfo.properties.entries.toList()) { entry ->
                    val key = entry.key
                    val value = entry.value
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween // 键值对分布在两端
                    ) {
                        Text(
                            text = "$key:",
                            modifier = Modifier.weight(1f) // Key 占据一部分空间
                        )
                        Text(
                            text = value,
                            modifier = Modifier.weight(2f) // Value 占据更多空间
                        )
                    }
                    HorizontalDivider()
                }



            }

        }
    }
}

suspend fun openSource(path: String) {
    withContext(Dispatchers.IO) {
        try {
            Desktop.getDesktop().open(File(path))
        } catch (e: Exception) {
            e.printStackTrace()
            // 可以选择在这里显示一个错误提示
        }
    }
}