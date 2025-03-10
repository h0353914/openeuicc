package im.angry.openeuicc.util

data class LPAString(
    val address: String,
    val matchingId: String?,
    val oid: String?,
    val confirmationCodeRequired: Boolean,
) {
    companion object {
        fun parse(input: String): LPAString {
            val components = input.removePrefix("LPA:").split('$')
            if (components.size < 2 || components[0] != "1") {
                throw IllegalArgumentException("Invalid activation code format")
            }
            return LPAString(
                address = components[1].trim(),
                matchingId = components.getOrNull(2)?.trim()?.ifBlank { null },
                oid = components.getOrNull(3)?.trim()?.ifBlank { null },
                confirmationCodeRequired = components.getOrNull(4)?.trim() == "1"
            )
        }
    }

    override fun toString(): String {
        val parts = arrayOf(
            "1",
            address,
            matchingId ?: "",
            oid ?: "",
            if (confirmationCodeRequired) "1" else ""
        )
        return parts.joinToString("$").trimEnd('$')
    }
}