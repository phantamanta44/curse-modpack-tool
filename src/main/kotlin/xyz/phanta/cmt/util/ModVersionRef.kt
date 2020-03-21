package xyz.phanta.cmt.util

sealed class ModVersionRef {
    companion object {
        fun parse(ref: String): ModVersionRef = ref.indexOf('@').let { vIndex ->
            if (vIndex == -1) {
                ModRef.parseGracefully(ref)?.let { OpenModVersionRef(it) }
            } else {
                ModRef.parseGracefully(ref.substring(0, vIndex))?.let {
                    ref.substring(vIndex + 1).toLongOrNull()?.let { version -> ClosedModVersionRef(it, version) }
                }
            } ?: throw IllegalArgumentException("Invalid mod version ref: $ref")
        }
    }

    abstract val modRef: ModRef
}

data class OpenModVersionRef(override val modRef: ModRef) : ModVersionRef() {
    override fun toString(): String = modRef.toString()
}

data class ClosedModVersionRef(override val modRef: ModRef, val fileId: Long) : ModVersionRef() {
    override fun toString(): String = "$modRef@$fileId"
}
