package com.example.ventasapp_ev2.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ventasapp_ev2.data.local.AppDatabase
import com.example.ventasapp_ev2.data.local.UsuarioEntity
import com.example.ventasapp_ev2.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = AuthRepository(AppDatabase.getDatabase(app).usuarioDao())

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    //actualizar el estado y limpiar el error del campo
    fun onUsuarioChange(valor: String) {
        _estado.update { it.copy(usuario = valor, usuarioError = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, claveError = null, errorGeneral = null) }
    }

    private fun validar(): Boolean {
        val e = _estado.value
        val usuarioError = if (e.usuario.isBlank()) "Ingrese su usuario" else null
        val claveError = when {
            e.clave.isBlank() -> "Ingrese su contraseña"
            e.clave.length < 4 -> "La contraseña debe tener al menos 4 caracteres"
            else -> null
        }
        _estado.update { it.copy(usuarioError = usuarioError, claveError = claveError) }
        return usuarioError == null && claveError == null
    }

    fun iniciarSesion(onExito: (UsuarioEntity) -> Unit) {
        if (!validar()) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            repository.sembrarUsuariosDePrueba()
            val usuario = repository.iniciarSesion(_estado.value.usuario, _estado.value.clave)
            if (usuario == null) {
                _estado.update {
                    it.copy(cargando = false, errorGeneral = "Usuario o contraseña incorrectos")
                }
            } else {
                _estado.update { it.copy(cargando = false) }
                onExito(usuario)
            }
        }
    }
}