package jay.desktopicon

class FileFinder {
    fun openFolder(directory: String): List<String>{
        val fileList = mutableListOf<String>()
        java.io.File(directory).walk().forEach {
            if (it.isFile) {
                fileList.add(it.absolutePath)
            }
        }
        return fileList
    }
    fun filterIcon(fileList: List<String>): List<String>{
        return fileList.stream().filter { item->item.endsWith(".desktop")
        }.toList();
    }

}