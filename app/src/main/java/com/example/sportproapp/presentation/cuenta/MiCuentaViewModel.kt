package com.example.sportproapp.presentation.cuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.remote.FirebaseAuthManager
import com.example.sportproapp.data.remote.supabase.EquipoRepository
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.example.sportproapp.data.session.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Hijo vinculado que se muestra en la sección "Mi familia"
data class HijoCuenta(
    val id: String,
    val nombre: String,
    val equipo: String?
)

data class MiCuentaUiState(
    val isLoading: Boolean = true,
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "",
    val hijos: List<HijoCuenta> = emptyList(),
    val equipoJugador: String? = null
)

// 2.06 Mi cuenta y 2.07 Cerrar sesión
class MiCuentaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MiCuentaUiState())
    val uiState: StateFlow<MiCuentaUiState> = _uiState.asStateFlow()

    fun cargarCuenta() {
        viewModelScope.launch {
            val sesion = UserSessionManager.getCurrentUser()
            val uid = sesion?.uid ?: FirebaseAuth.getInstance().currentUser?.uid

            if (uid.isNullOrBlank()) {
                _uiState.value = MiCuentaUiState(isLoading = false)
                return@launch
            }

            // Si la caché está vacía, se leen los datos del usuario desde Supabase
            var nombre = sesion?.name ?: ""
            var email = sesion?.email ?: ""
            var rol = sesion?.role ?: ""
            if (sesion == null) {
                UsuarioRepository.getUser(uid).getOrNull()?.let { usuario ->
                    nombre = usuario.name
                    email = usuario.email
                    rol = usuario.role
                }
            }

            var hijos = emptyList<HijoCuenta>()
            var equipoJugador: String? = null

            when (rol) {
                "PAD" -> {
                    val equipos = cargarEquipos()
                    hijos = UsuarioRepository.getMisRepresentadosConUsuario(uid).getOrDefault(emptyList())
                        .map { jugador ->
                            HijoCuenta(
                                id = jugador.perfil.id,
                                nombre = jugador.usuario.name,
                                equipo = nombreEquipo(equipos, jugador.perfil.equipoId)
                            )
                        }
                }
                "JUG" -> {
                    val perfil = UsuarioRepository.getPlayerProfile(uid).getOrNull()
                    if (perfil?.equipoId != null) {
                        equipoJugador = nombreEquipo(cargarEquipos(), perfil.equipoId)
                    }
                }
            }

            _uiState.value = MiCuentaUiState(
                isLoading = false,
                uid = uid,
                nombre = nombre,
                email = email,
                rol = rol,
                hijos = hijos,
                equipoJugador = equipoJugador
            )
        }
    }

    private suspend fun cargarEquipos(): List<Equipo> =
        EquipoRepository.getEquipos().getOrDefault(emptyList())

    private fun nombreEquipo(equipos: List<Equipo>, equipoId: String?): String? {
        val equipo = equipos.firstOrNull { it.id == equipoId } ?: return null
        return listOf(equipo.nombre, equipo.categoria).filter { it.isNotBlank() }.joinToString(" ")
    }

    // Cierra la sesión de Firebase y limpia los datos guardados en memoria
    fun cerrarSesion() {
        FirebaseAuthManager.logout()
        UserSessionManager.clearSession()
    }
}
