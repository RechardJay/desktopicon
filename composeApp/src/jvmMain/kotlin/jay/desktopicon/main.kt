package jay.desktopicon

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

val homeDirectory: String? = System.getProperty("user.home")
var iconDirectory = listOf(
    "/usr/share/applications/",
    "$homeDirectory/.local/share/applications/"
)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "desktopicon",
    ) {
        App()
    }
}