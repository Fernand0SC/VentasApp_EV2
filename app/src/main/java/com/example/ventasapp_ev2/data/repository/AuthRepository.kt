package com.example.ventasapp_ev2.data.repository

import com.example.ventasapp_ev2.data.local.UsuarioDao
import com.example.ventasapp_ev2.data.local.UsuarioEntity
import com.example.ventasapp_ev2.data.model.Perfil
import java.security.MessageDigest

class AuthRepository(private val usuarioDao: UsuarioDao) {

    // Cuentas Prueba
    suspend fun sembrarUsuariosDePrueba() {
        if (usuarioDao.contar() == 0) {
            usuarioDao.insertar(
                UsuarioEntity(
                    nombre = "Vendedor Demo",
                    usuario = "vendedor",
                    claveHash = hash("1234"),
                    perfil = Perfil.VENDEDOR.name
                )
            )
            usuarioDao.insertar(
                UsuarioEntity(
                    nombre = "Encargado Demo",
                    usuario = "encargado",
                    claveHash = hash("1234"),
                    perfil = Perfil.ENCARGADO.name
                )
            )
        }
    }

    // Devuelve el usuario si las credenciales son correctas, o null si no
    suspend fun iniciarSesion(usuario: String, clave: String): UsuarioEntity? {
        val encontrado = usuarioDao.buscarPorUsuario(usuario.trim()) ?: return null
        return if (encontrado.activo && encontrado.claveHash == hash(clave)) encontrado else null
    }


    private fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }
}