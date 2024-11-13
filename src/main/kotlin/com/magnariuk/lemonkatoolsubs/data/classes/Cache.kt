package com.magnariuk.lemonkatoolsubs.data.classes

import com.google.gson.annotations.Expose

data class Cache(
    var actors: MutableList<Actor> = mutableListOf(),
    var resultFolderPath: String = "",
    var removeUsedCharacters: Boolean = false,
    )

data class Actor(
    var actorName: String = "",
    var characterNames: MutableList<String> = mutableListOf(),
)
/*data class Settings(

)*/