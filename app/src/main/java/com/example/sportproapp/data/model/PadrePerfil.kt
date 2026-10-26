import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Model para la tabla padres_apoderados
@Serializable
data class PadrePerfil(
    @SerialName("id") val id: String,
    @SerialName("direccion") val direccion: String
)