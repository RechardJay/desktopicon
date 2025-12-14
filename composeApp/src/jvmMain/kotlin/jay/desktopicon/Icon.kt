package jay.desktopicon

import java.io.File

data class Icon (
    val path: String
){
    var noDisplay: Boolean = false
    lateinit var other: Map<String, String>
    lateinit var name: String
    lateinit var icon: String

    constructor(file: File) : this(path = file.path) {
        val lines = file.readLines()
        val content = mutableMapOf<String, String>()
        for (line in lines) {
            if(!line.contains("=")) continue
            line.split('=').let {
                content[it[0]] = it[1]
            }
        }
        if(content.containsKey("Name")){
            name = content["Name"].toString()
            content.remove("Name")
        }
        if(content.containsKey("Icon")){
            icon = content["Icon"].toString()
            content.remove("Icon")
        }
        if (content.contains("NoDisplay")){
            noDisplay = content["NoDisplay"].toBoolean()
        }
        other = content

    }
}