# VentasApp – Gestión móvil del registro y validación de ventas

Aplicación Android desarrollada para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC), Evaluación Parcial 2.

## Integrantes

- Fernando Serrano
- Manuel Gaete

**Equipo:** Grupo 2

## Descripción

Producto Mínimo Viable que permite a los vendedores registrar ventas desde su teléfono, con su respaldo fotográfico, y consultarlas o modificarlas mientras no estén validadas. Reemplaza el proceso manual de fotografías por mensajería y planillas por un registro centralizado y persistente.

**Todos los datos son ficticios**: no se usan clientes, comprobantes ni montos reales, y la app no se conecta a sistemas externos.

## Funcionalidades de la Evaluación Parcial 2

- Inicio de sesión con cuentas de prueba y menú principal.
- Navegación entre las pantallas Login → Menú → Lista de ventas → Formulario, con transiciones animadas.
- **Lista de ventas** almacenadas localmente, con código, tipo de servicio, monto y estado, actualizada automáticamente al registrar o modificar.
- **Formulario de venta** (crear y modificar) con: tipo de convenio o servicio, fecha, cliente ficticio, monto, código de autorización y observaciones.
- **Validaciones** con error visible e ícono en cada campo: campos obligatorios, fecha válida, monto numérico mayor que 0 y código de autorización no duplicado. La lógica de validación está en el ViewModel y en validadores independientes de la interfaz.
- Estado inicial de la venta como **Borrador** o **Recibida**.
- **Persistencia local con Room**: las ventas se conservan al cerrar y reiniciar la aplicación.
- **Recursos nativos**: respaldo fotográfico ficticio mediante la **cámara** o la **galería**, con restricción de tipo (JPG, PNG, WebP) y tamaño máximo de 8 MB. La imagen queda vinculada a la venta.
- Las ventas validadas quedan en solo lectura.

## Funcionalidades adicionales

Adelantos del caso completo, que no forman parte de la calificación de esta evaluación:

- Perfil **encargado**: lista de ventas recibidas, detalle con respaldo fotográfico y acciones de validar, rechazar o solicitar corrección (con observación obligatoria).
- Historial de cambios por venta (acción, estados, usuario, fecha y observación).
- Visualización de la observación del encargado para el vendedor.
- Claves almacenadas como hash (SHA-256).

## Arquitectura

Patrón **MVVM** con estado inmutable (`UiState`) expuesto mediante `StateFlow`. Las pantallas solo muestran el estado y envían eventos; la validación y las reglas de negocio no viven en la interfaz. Los ViewModels acceden a los datos únicamente a través de los repositorios.

```
app/src/main/java/com/example/ventasapp_ev2/
├── data/
│   ├── local/        Entidades, DAOs y AppDatabase (Room)
│   ├── model/        Enumeraciones de dominio (Perfil, EstadoVenta)
│   └── repository/   Repositorios (acceso a datos y reglas de negocio)
├── validation/       Validadores de login, venta y revisión (lógica pura, independiente de la interfaz)
├── ui/
│   ├── components/   Componentes reutilizables
│   ├── login/        Pantalla, estado y ViewModel de inicio de sesión
│   ├── menu/         Menú principal
│   ├── venta/        Lista y formulario de ventas (vendedor)
│   ├── revision/     Lista, detalle y revisión de ventas (encargado)
│   ├── navigation/   Rutas y NavHost
│   └── theme/        Tema Material 3
├── utils/            Manejo de archivos de respaldo (cámara y galería)
└── MainActivity.kt
```

## Tecnologías

- Kotlin
- Jetpack Compose y Material Design 3
- Arquitectura MVVM (ViewModel, StateFlow)
- Room (persistencia local)
- Navigation Compose
- Recursos nativos: cámara y galería de imágenes
- Coil (visualización de imágenes)

## Cómo ejecutar el proyecto

1. Clonar el repositorio:

```bash
   git clone https://github.com/Fernand0SC/VentasApp_EV2.git
```

2. Abrir la carpeta en **Android Studio**.
3. Esperar a que termine la sincronización de Gradle (*Sync Project with Gradle Files*).
4. Ejecutar la app en un emulador o en un teléfono Android (mínimo Android 7.0, API 24).
5. Aceptar el permiso de cámara cuando la app lo solicite.

## Cuentas de prueba (ficticias)

| Perfil    | Usuario     | Contraseña |
|-----------|-------------|------------|
| Vendedor  | `vendedor`  | `1234`     |
| Encargado | `encargado` | `1234`     |

## Flujo de prueba sugerido

1. Entrar como **vendedor** y crear una venta con respaldo fotográfico (cámara o galería).
2. Guardarla como borrador o enviarla, y comprobar que aparece en la lista.
3. Abrirla, modificar algún dato y guardar.
4. Cerrar y reabrir la app: las ventas siguen en la lista.
5. (Adicional) Entrar como **encargado**, revisar la venta y consultar su historial.

## Planificación

Tablero de tareas en Trello: https://trello.com/b/42b2dj5N/ventasapp-dsy1105