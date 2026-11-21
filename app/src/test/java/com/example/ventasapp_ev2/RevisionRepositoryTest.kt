package com.example.ventasapp_ev2

import com.example.ventasapp_ev2.data.local.HistorialDao
import com.example.ventasapp_ev2.data.local.UsuarioDao
import com.example.ventasapp_ev2.data.local.UsuarioEntity
import com.example.ventasapp_ev2.data.local.VentaDao
import com.example.ventasapp_ev2.data.model.EstadoVenta
import com.example.ventasapp_ev2.data.repository.ReglaNegocioException
import com.example.ventasapp_ev2.data.repository.RevisionRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class RevisionRepositoryTest : StringSpec({

    "un vendedor no puede validar una venta" {

        runTest {

            val ventaDao = mockk<VentaDao>()
            val usuarioDao = mockk<UsuarioDao>()
            val historialDao = mockk<HistorialDao>()

            val vendedor = UsuarioEntity(
                id = 1,
                nombre = "Vendedor Demo",
                usuario = "vendedor",
                claveHash = "hash",
                perfil = "VENDEDOR"
            )

            coEvery { usuarioDao.obtenerPorId(1) } returns vendedor

            val repository = RevisionRepository(ventaDao, usuarioDao, historialDao)

            val error = shouldThrow<ReglaNegocioException> {
                repository.cambiarEstado(
                    ventaId = 10,
                    nuevoEstado = EstadoVenta.VALIDADA,
                    responsableId = 1,
                    observacion = null
                )
            }

            error.message shouldBe "Solo el encargado puede revisar ventas"

            coVerify(exactly = 0) { ventaDao.actualizar(any()) }
        }
    }
})