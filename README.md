# MI-AGENDA

Agenda personal para el teléfono. Sirve para la universidad, los proyectos, los hábitos y el descanso. La versión de esta carpeta es **0.1.0** (`versionCode` 1). El identificador de la aplicación es `com.miagenda.app`.

Todo se guarda en el teléfono. No hay cuentas, internet, anuncios ni ubicación. La copia de seguridad es un archivo JSON sin cifrado: quien pueda abrirlo puede leerlo.

## Qué puedes hacer

- Ver el día y la semana, de lunes a domingo. La barra inferior tiene la sección Horario: la tabla semanal, en columnas o en filas, y se puede girar el teléfono. Desde ahí, o con el botón +, se añade una clase de universidad y se repite cada semana. Calendario también abre esa tabla. No hay vista de mes.
- Crear actividades, con repetición, y categorías. Si borras una categoría, las actividades, tareas y hábitos se quedan sin categoría.
- Crear tareas y hábitos, marcarlos y ver el porcentaje de un hábito.
- Elegir el color de acento y el tema. Las letras son claras en el tema oscuro y oscuras en el tema claro.
- Ver a Luma, el gatito, en Inicio debajo del saludo. Crece al completar actividades, tareas y hábitos: dormido, despierto, con un libro y con una estrella. No retrocede. En Ajustes se puede ocultar.
- Escribir la reflexión del día y ver el resumen de la semana.
- Exportar y restaurar una copia desde Ajustes. Restaurar sustituye lo que hay en el teléfono. No se puede deshacer.

## Qué no incluye esta versión

No hay notificaciones, widgets ni temporizador. El archivo de copia no va cifrado. Luma se dibuja en la propia app, sin imágenes descargadas. Las pruebas se ejecutaron en este PC, no en un teléfono.

## Este equipo

En este PC ya están instalados:

- JDK Temurin 25.0.3, en `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`
- Android SDK, en `C:\Android\Sdk`, con la plataforma `platforms\android-37.0`
- El proyecto, en `C:\Users\MI-AGENDA`

Android Studio no hace falta para generar el APK. El archivo `local.properties` no está en GitHub. En este PC dice `sdk.dir=C:\\Android\\Sdk`. Si clonas el repositorio en otro equipo, crea ese archivo con la ruta de tu SDK.

## Generar el APK

Abre PowerShell en `C:\Users\MI-AGENDA` y ejecuta:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug --console=plain
```

Si termina bien, la última línea dice `BUILD SUCCESSFUL`. El APK queda en:

`C:\Users\MI-AGENDA\app\build\outputs\apk\debug\app-debug.apk`

La última generación de este archivo en este PC fue el 25 de octubre de 2026 a las 23:33. Pesaba 26529464 bytes. En esa misma ejecución hubo **74 pruebas y 0 fallos**. El informe de Gradle marca la hora `2026-10-26T03:33:56Z`.

Esa generación incluye la sección Horario, las clases de universidad, a Luma y el cambio de color de las letras. `adb install -r` terminó en Success. No comprobé si las pantallas se abrieron en el teléfono.

Si aparece `BUILD FAILED`, el error está más arriba en la misma ventana. No uses el APK viejo hasta que esa orden termine bien.

## Instalarlo en el teléfono

El teléfono necesita Android 8 o posterior. La copia de las 23:33 ya se instaló con `adb install -r`. Para volver a instalarla, conecta el teléfono y ejecuta las dos órdenes de abajo. Si `adb devices` sale vacío, el teléfono no está conectado.

1. En el teléfono, activa las opciones de desarrollador y la depuración USB. El nombre del menú cambia según la marca. Suele estar en Ajustes, dentro de Acerca del teléfono, tocando varias veces el número de compilación.
2. Conecta el teléfono por USB y acepta el aviso de depuración en la pantalla del teléfono.
3. En PowerShell ejecuta:

```powershell
& "C:\Android\Sdk\platform-tools\adb.exe" devices
& "C:\Android\Sdk\platform-tools\adb.exe" install -r "C:\Users\MI-AGENDA\app\build\outputs\apk\debug\app-debug.apk"
```

`devices` debe mostrar una línea con el teléfono y la palabra `device`. Si dice `unauthorized`, acepta el aviso en el teléfono y vuelve a ejecutar las dos órdenes. Si la lista sale vacía, el cable no está en modo datos o la depuración USB sigue apagada.

La instalación correcta termina con `Success`. Después abre **Mi Agenda** en el teléfono. Es una versión de depuración, no una versión de tienda.
