package xyz.phanta.cmt.model

enum class ModLoader(val id: String) {
    FORGE("forge"); // TODO add other modloaders

    override fun toString(): String = id
}
