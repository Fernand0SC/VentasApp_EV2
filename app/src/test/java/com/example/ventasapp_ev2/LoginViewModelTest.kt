package com.example.ventasapp_ev2

import android.app.Application
import com.example.ventasapp_ev2.data.repository.AuthRepository
import com.example.ventasapp_ev2.ui.login.LoginViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : StringSpec({

    val dispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(dispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    "iniciarSesion con credenciales incorrectas debe mostrar el mensaje de error" {

        runTest(dispatcher) {

            val fakeRepository = mockk<AuthRepository>()

            coEvery { fakeRepository.sembrarUsuariosDePrueba() } just Runs
            coEvery { fakeRepository.iniciarSesion("vendedor", "0000") } returns null

            val testViewModel = LoginViewModel(mockk<Application>(relaxed = true), fakeRepository)

            testViewModel.onUsuarioChange("vendedor")
            testViewModel.onClaveChange("0000")
            testViewModel.iniciarSesion { }

            advanceUntilIdle()

            testViewModel.estado.value.errorGeneral shouldBe "Usuario o contraseña incorrectos"
            testViewModel.estado.value.cargando shouldBe false
        }
    }
})