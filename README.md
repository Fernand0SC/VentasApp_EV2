\# VentasApp – Gestión móvil del registro y validación de ventas



Aplicación Android desarrollada para la asignatura \*\*DSY1105 – Desarrollo de Aplicaciones Móviles\*\* (Duoc UC), Evaluación Parcial 2.



\## Integrantes

\- Fernando Serrano

\- Manuel Gaete



\*\*Equipo:\*\* Grupo 2



\## Descripción

Producto Mínimo Viable que permite a los vendedores registrar ventas desde su teléfono y a la persona encargada revisarlas, validarlas o devolverlas con observaciones. Reemplaza el proceso manual de fotografías por mensajería y planillas por un registro centralizado, con estados y trazabilidad.



\## Funcionalidades implementadas



\*\*General\*\*

\- Inicio de sesión con cuentas de prueba y control de acceso por perfil (Vendedor / Encargado).

\- Claves almacenadas como hash, no como texto visible.

\- Menú principal distinto según el perfil.



\*\*Perfil vendedor\*\*

\- Lista de ventas propias, actualizada automáticamente al registrar o modificar.

\- Formulario de venta (crear y editar) con: tipo de convenio o servicio, fecha, cliente ficticio, monto, código de autorización y observaciones.

\- Validaciones con error visible en cada campo: obligatorios, monto numérico mayor que 0 y código de autorización no duplicado.

\- Guardado como \*\*Borrador\*\* o envío como \*\*Recibida\*\*.

\- Respaldo fotográfico ficticio mediante \*\*cámara\*\* o \*\*galería\*\*, con restricción de tipo y tamaño de archivo.

\- Las ventas validadas quedan en solo lectura.



\*\*Perfil encargado\*\*

\- Lista de ventas recibidas.

\- Detalle de cada venta con sus antecedentes y respaldo fotográfico.

\- Acciones: \*\*validar\*\*, \*\*rechazar\*\* y \*\*solicitar corrección\*\*, con observación obligatoria al rechazar o corregir.

\- Regla de negocio: no se puede revisar una venta propia ni una venta que no esté en estado Recibida.



\*\*Trazabilidad\*\*

\- Historial de cambios por venta: acción realizada, estado anterior y nuevo, usuario responsable, fecha y hora, y observación.



\## Arquitectura

Patrón \*\*MVVM\*\* con estado inmutable (`UiState`) expuesto mediante `StateFlow`. La validación de campos vive en los ViewModels, no en las pantallas.



```

app/src/main/java/com/example/ventasapp\_ev2/

├── data/

│   ├── local/        Entidades, DAOs y AppDatabase (Room)

│   ├── model/        Enumeraciones de dominio (Perfil, EstadoVenta)

│   └── repository/   Repositorios (acceso a datos y reglas de negocio)

├── ui/

│   ├── login/        Pantalla y ViewModel de inicio de sesión

│   ├── menu/         Menú principal

│   ├── venta/        Lista y formulario de ventas (vendedor)

│   ├── revision/     Lista, detalle y revisión de ventas (encargado)

│   └── navigation/   Rutas y NavHost

├── utils/            Manejo de archivos de respaldo

└── MainActivity.kt

```



\## Tecnologías

\- Kotlin

\- Jetpack Compose y Material Design 3

\- Arquitectura MVVM (ViewModel, StateFlow)

\- Room (persistencia local)

\- Navigation Compose

\- Recursos nativos: cámara y galería de imágenes

\- Coil (visualización de imágenes)



\## Cómo ejecutar el proyecto

1\. Clonar el repositorio:

```bash

&#x20;  git clone https://github.com/Fernand0SC/VentasApp\_EV2.git

```

2\. Abrir la carpeta en \*\*Android Studio\*\*.

3\. Esperar a que termine la sincronización de Gradle (\*Sync Project with Gradle Files\*).

4\. Ejecutar la app en un emulador o en un teléfono Android (mínimo Android 7.0, API 24).

5\. Aceptar el permiso de cámara cuando la app lo solicite.



\## Cuentas de prueba (ficticias)



| Perfil | Usuario | Contraseña |

|---|---|---|

| Vendedor | `vendedor` | `1234` |

| Encargado | `encargado` | `1234` |



\## Flujo de prueba sugerido

1\. Entrar como \*\*vendedor\*\*, crear una venta con respaldo fotográfico y enviarla.

2\. Cerrar sesión y entrar como \*\*encargado\*\*.

3\. Abrir la venta en \*\*Ventas recibidas\*\* y validarla, rechazarla o solicitar corrección.

4\. Revisar el \*\*historial de cambios\*\* al final del detalle.



\## Planificación

Tablero de tareas en Trello: https://trello.com/b/42b2dj5N/ventasapp-dsy1105

