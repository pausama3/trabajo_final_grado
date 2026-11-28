# Registro de cambios · MedManager

Cada entrada explica qué ha cambiado, por qué y cómo se prueba. Las más recientes van arriba.

---

## 27/11/2026 · Pruebas de base de datos: `Test.sql` y memoria §8.1 coherentes

### Cambios

- `database/Files SQL/Test.sql` reescrito en el orden de la memoria: cita correcta de Ana
  (`@pac1`) con `medico1` y `SET @cita = LAST_INSERT_ID()` justo después; PBD-02 Pedro
  (`@pac2`) con el mismo médico; PBD-02b Ana con `medico2`; PBD-03 cancelar `@cita` y repetir
  el INSERT de Pedro; PBD-04a/04b con el enfermero; `ROLLBACK`. Cada prueba lleva un comentario
  con el error esperado. Fuera `@rec` y las sentencias repetidas del final.
- Memoria §8.1 con el mismo código y textos que el script: variables sin `@rec` ni `@cita`,
  INSERT de Ana y de Pedro por separado en PBD-02, `UPDATE … 'CANCELADA'` en PBD-03 y títulos
  de PBD-04a/04b solo con el enfermero.
- Pendiente: repetir las capturas de PBD-02, PBD-02b y PBD-03 con el script nuevo.

### Cómo se prueba

Ejecutar `Test.sql` sentencia a sentencia en Workbench y comprobar que cada error coincide con
el comentario: 3819, 1062 (`uq_cita_medico_franja`), 1062 (`uq_cita_paciente_franja`), OK,
1644, 1644.

---

## 27/11/2026 · Memoria §8.1 (pruebas de base de datos): erratas corregidas

### Cambios

- `docs/Entregas/DAM Plantilla memoria escrita.docx`, §8.1:
  - Ortografía: «tenia» → «tenía», «medico» → «médico», «como» → «cómo», «inster» → «INSERT»,
    espacio antes de coma y doble espacio eliminados.
  - Redacción: tiempos verbales unificados en el párrafo de START TRANSACTION, persona
    impersonal en todo el apartado y reescritos los párrafos de PBD-02b, PBD-03 y PBD-04a
    (ahora citan `uq_cita_paciente_franja` y `trg_cita_bi`).
  - Títulos de las pruebas con el mismo formato «PBD-xx: texto» (PBD-01 y PBD-02 iban al final).
  - PBD-04b completado: el `INSERT` con `@rec` (variable que no existía) pasa a ser
    `UPDATE cita SET id_trabajador = @enf WHERE id_cita = @cita;`. El texto explica que
    `trg_cita_bu` lo rechaza con el error 1644 (SQLSTATE 45000).

---

## 26/11/2026 · Kanban y Gantt día a día actualizados

### Cambios

- `docs/Kanban MedManager.html`:
  - Pasan a «Hecho»: paleta Android igual que el escritorio, Gantt simplificado, §6.6 de la
    memoria (diagrama de clases Android) y esta actualización.
  - La tarjeta «diagrama de clases de Android y wireframes finales» se queda solo con los
    wireframes (§7.1 «BOCETOS FIGMA» y la fila de Figma del §4.2, que aún cita el escáner QR).
  - Tareas nuevas: dirección del servidor configurable en Android (28/11) y redactar la interfaz
    Android del §7.4 y rehacer sus capturas con la paleta nueva (29/11). La de los márgenes del
    login incluye ahora compilar con la paleta nueva.
  - Replanificadas: observaciones como médico y márgenes del login → 27/11; CU-A02, figura del
    Gantt §5.4, enlace Kroki y JAR → 28/11.
  - La clave de `localStorage` pasa a `medmanager-kanban-v8`: los cambios hechos a mano en el
    navegador sobre la versión anterior no se conservan.
- `docs/Diagramas/Diagrama de Gantt dia a dia.xlsx`:
  - Fila nueva «Tarea 3.4: diagramas de clases actualizados y Android» (19/11–26/11, 100 %).
  - «JAR ejecutable y comprobación de las observaciones como médico» pasa al 27–28/11, y la fase
    de escritorio termina el 28/11.

---

## 26/11/2026 · Memoria §6.6: capa de cliente móvil (Android) y su diagrama de clases

### Motivo

En el §6.6 (Diagramas de clases), el apartado «Capa de Cliente Móvil (Android / Kotlin)» solo tenía
el título. El resto de capas (dominio, servidor y escritorio) ya tenían texto y diagrama.

### Cambios

- `docs/Diagramas/diagramas_clases_fuente.html`: nuevo diagrama `#android`, con el mismo estilo que
  los otros y un color morado propio (`mov`). Incluye las 4 actividades y su herencia de
  `AppCompatActivity`, la navegación con `Intent`, `PacienteAdapter`, `ClienteSocket` (`object`),
  `ErrorServidorException`, las funciones de `Utilidades.kt` y el paquete `comun`.
- `docs/Diagramas/Diagrama de clases Cliente Android.png`: el diagrama exportado a 2760×1780 px
  (escala ×2, como los demás) con Edge en modo headless.
- `docs/Entregas/DAM Plantilla memoria escrita.docx`, §6.6 «Capa de Cliente Móvil»:
  - Un párrafo de introducción y 6 viñetas con el mismo formato que la capa de escritorio:
    `ClienteSocket`, paquete `comun`, actividades (CU-E01 a CU-E04), `PacienteAdapter`,
    `Utilidades.kt` y corrutinas.
  - La figura del diagrama con su pie: «Figura: Diagrama de clases de la aplicación Android
    (elaboración propia)».

### Cómo comprobarlo

- Abrir la memoria en Word y actualizar el índice: el §6.6 crece casi una página y desplaza los
  números de página siguientes.
- Para regenerar el PNG, abrir `diagramas_clases_fuente.html#android` en el navegador.

---

## 25/11/2026 · Gantt día a día simplificado con las tareas del pronóstico

### Motivo

El Gantt día a día tenía 63 filas, muchas de ellas mini tareas (arreglos, revisiones de una tarde).
Así no se podía comparar con el pronóstico. Ahora tiene las mismas fases y tareas que el pronóstico,
con las fechas reales sacadas de los commits de git y del trabajo hecho.

### Cambios

- `docs/Diagramas/Diagrama de Gantt dia a dia.xlsx`: pasa a 35 filas en 5 fases, las mismas que el
  pronóstico.
  - **Documentación y Diseño (22/09–11/12):** propuesta (22/09–05/10), diagramas (24–27/09),
    Tarea 1 (18–28/10), Tarea 3.1 (29/10–02/11), Tarea 2 (02–22/11), entrega para feedback (05/11)
    y §7 de la memoria (13–25/11), todas al 100 %. Pendientes: metodología y capturas (5–6/12),
    Tarea 4 (7/12), Tarea 5 (8/12) y Tarea 6, vídeo y entrega (9–11/12).
  - **Base de Datos (08/10–27/11):** DDL, restricciones y triggers, usuarios y datos de prueba
    hechos. Pendiente: verificación de triggers (27/11).
  - **Servidor (08/10–02/12):** Hibernate, ORM, ServerSocket, protocolo y seguridad, DAO y acciones
    hechos. Pendiente: concurrencia y JUnit (27/11–02/12).
  - **Escritorio (15/11–27/11):** ventanas, conexión y módulos hechos. Pendiente: JAR y comprobar
    las observaciones como médico (26–27/11).
  - **Android (22/11–04/12):** layouts con la paleta del escritorio, corrutinas, ficha y
    observaciones hechos. Pendiente: pruebas de integración (04/12).
- Las mini tareas desaparecen como filas propias y quedan dentro de la tarea grande a la que
  pertenecen. Por ejemplo, los arreglos de `hibernate.cfg.xml` entran en «DAO y acciones», y el
  arreglo de la compilación Android en «Diseño de layouts».

### Cómo comprobarlo

Abrir el Excel: las barras grises (hechas) coinciden con las fechas de `git log` y las pendientes
empiezan a partir del 26/11. La captura `Diagrama de Gantt real 19-11-2026.png` de la memoria (§5.4)
es anterior y habrá que rehacerla al final.

---

## 25/11/2026 · App Android con la misma paleta de colores que el escritorio

### Motivo

Las dos aplicaciones deben tener la misma imagen. La app Android usaba los colores por defecto de
Material 3 (morado), y el escritorio tiene su paleta en `Estilo.java`. Ese fichero no se puede
reutilizar en Android porque depende de `java.awt.Color` y de Swing, así que se copian sus valores.

### Cambios

- `res/values/colors.xml`: se añaden los colores de `Estilo.java` con los mismos valores hexadecimales
  (`azul_oscuro`, `azul_hover`, `acento`, `fondo`, `blanco`, `texto`, `texto_suave`, `linea`), más
  `acento_claro` y `fila_alterna`, que el escritorio usa en las tablas. Un comentario recuerda que,
  si se cambia la paleta en `Estilo.java`, hay que cambiarla también aquí.
- `res/values/themes.xml`: el tema pasa a `Theme.Material3.Light.NoActionBar` y asigna la paleta a
  los roles de Material 3: primario = azul oscuro, secundario = acento, fondo = `fondo`,
  superficies = blanco, texto = `texto`/`texto_suave`, bordes = `linea`, error = `alerta`.
  Los layouts ya usan los estilos de Material, así que botones, campos y tarjetas cogen los colores
  sin tocar los XML.
- Botones rellenos con el color de acento, igual que `Estilo.botonAccion()`: estilo
  `Widget.EnfermeriaMedManager.BotonAccion` en el tema y nuevo `res/color/boton_accion.xml`
  (acento cuando el botón está activo, gris `linea` cuando está desactivado). Los botones de texto
  siguen en azul oscuro.
- Se borra `res/values-night/themes.xml`: en modo oscuro sustituía el tema sin colores y la paleta
  se perdía. La app queda siempre en modo claro, como el escritorio.
- No se copian las fuentes: Segoe UI no existe en Android y se mantiene la fuente del sistema (Roboto).
- `docs/Entregas/DAM Plantilla memoria escrita.docx`: en §7.4 Interfaz → «Aplicación Android» se
  añade un párrafo al final que explica que la app usa la paleta de `Estilo` copiada a `colors.xml`
  y aplicada en el tema de Material 3. Las capturas de ese apartado son anteriores al cambio de
  colores y habrá que rehacerlas.

### Cómo probarlo

1. Compilar y ejecutar la app en el emulador.
2. Los botones rellenos se ven en verde azulado (acento), los botones de texto y el borde de los campos con foco
   en azul oscuro, y el fondo de las pantallas es el gris azulado del escritorio.
3. Activar el modo oscuro del teléfono: la app debe seguir viéndose igual.

**No se ha compilado con Gradle.**

---

## 25/11/2026 · Kanban y Gantt día a día actualizados

### Motivo

El Kanban y el Gantt se habían actualizado por última vez el 22/11. Desde entonces se han hecho las
acciones de enfermería del servidor y toda la app Android, que estaba planificada del 29/11 al 3/12.

### Cambios

- `docs/Kanban MedManager.html`:
  - Pasan a «Hecho»: acciones de enfermería del servidor, las tres tareas de Android, limpieza del
    `.gitignore`, revisión del flujo de enfermería y apartados 7.2/7.3 de la memoria.
  - Tareas nuevas pendientes: comprobar en el escritorio que el médico ve las observaciones,
    márgenes del login Android, figura del Gantt real (§5.4), enlace Kroki y `Mod Observaciones.sql`.
  - Las tareas vencidas (CU-A02, triggers, concurrencia, JAR) se replanifican al 26-27/11.
  - La clave de `localStorage` pasa a `medmanager-kanban-v7`, así que el tablero carga la lista
    nueva. Los cambios hechos a mano en el navegador sobre la versión anterior no se conservan.
- `docs/Diagramas/Diagrama de Gantt dia a dia.xlsx`:
  - Fase Android con fechas reales (22/11-25/11) y progreso 100 %; se añaden el arreglo de la
    compilación y el login con corrutinas.
  - Servidor: `hibernate.cfg.xml` (22/11) y acciones de enfermería (24-25/11) al 100 %.
  - Escritorio: prueba de «Reprogramar cita» y filtro por paciente (22/11) al 100 %.
  - Las tareas pendientes vencidas se mueven al 26-27/11, y se añaden filas para las nuevas tareas
    pendientes. Las fases de pruebas, memoria y entrega no cambian.
  - Se ha editado el XML de la hoja directamente para conservar la barra de progreso y los
    formatos condicionales de la plantilla.

### Desviación respecto a la planificación

La app Android se ha terminado una semana antes de lo previsto. Quedan atrasadas varias pruebas
(triggers, concurrencia) y el JAR ejecutable, que estaban previstas del 23 al 25/11.

---

## 25/11/2026 · App Android: login con corrutinas, ClienteSocket en `red/` y memoria (7.2 y 7.3)

### Motivo

- **Login con corrutinas:** era la única pantalla que usaba un `Thread` con `runOnUiThread`. Las demás
  ya usaban corrutinas, y la memoria dice que la app usa corrutinas para la red. Ahora toda la app
  funciona igual.
- **ClienteSocket en `red/`:** el fichero declaraba el paquete `...enfermeriamedmanager.red` pero
  estaba en la carpeta raíz del paquete. Ahora la carpeta coincide con el paquete y con lo que explica
  la memoria.
- **Memoria:** faltaba el apartado de la app Android en 7.3 (Estructura interna) y las acciones de
  enfermería en la tabla del 7.2.

### Cambios

- `MainActivity.kt`: el clic de «Entrar» usa `lifecycleScope.launch` y `withContext(Dispatchers.IO)`,
  como el resto de pantallas.
  - Los errores de red se capturan como `IOException` (antes era `Exception`, que con corrutinas
    también atraparía la cancelación).
  - El botón se reactiva en el `finally`.
  - El comportamiento es el mismo: usuarios que no son de enfermería rechazados, paso al buscador,
    mensajes de error.
- `ClienteSocket.kt` se mueve con `git mv` a `app/src/main/java/com/medmanager/enfermeriamedmanager/red/`.
  No cambia ni el paquete ni los imports.
- `docs/Entregas/DAM Plantilla memoria escrita.docx`: los cambios van **como control de cambios**
  (autor «Claude»), para que el autor de la memoria los revise y los acepte o rechace en Word:
  - **7.2, tabla de acciones:** dos filas nuevas, `CITAS_PACIENTE_HOY` y `GUARDAR_OBSERVACIONES`. Se
    borra el `[PENDIENTE]` de debajo.
  - **7.3, App android:**
    - Después de «Ponemos el nombre de la aplicación…», dos párrafos sobre Kotlin, minSdk 26 y las
      dependencias.
    - Un punto nuevo «Paquetes», con los tres paquetes.
    - La descripción de cada clase bajo «Clases», y los ficheros bajo «Archivos».
    - Un punto nuevo «Comunicación con el servidor en segundo plano», con las corrutinas y el
      tratamiento de errores.
    - Se borra el `[PENDIENTE]` de la estructura Android.
  - Se usa el mismo formato que en el apartado del escritorio: lista numerada para los puntos y
    viñetas «•» y «o» para paquetes, clases y ficheros.

### Comprobaciones realizadas

- `gradlew assembleDebug` compila sin errores.
- Login probado en el emulador:
  - Con una contraseña incorrecta sale «Usuario o contraseña incorrectos».
  - Con `medico1` sale «Esta app es solo para enfermería» y se queda en el login.
  - Con `enfermero1` entra al buscador.
- Memoria:
  - El `document.xml` resultante es XML válido.
  - Word abre el documento sin avisos, con 7 revisiones de «Claude» (5 inserciones y 2 eliminaciones)
    y 64 páginas.
  - Se ha comprobado el orden de los párrafos insertados.
  - Los cambios se aplicaron sobre la versión guardada a las 12:42, cuando ya se había cerrado Word.
    Hay una copia de esa versión sin las revisiones fuera del repositorio, por si hiciera falta
    volver atrás.

---

## 25/11/2026 · App Android: pantalla 4, observaciones de enfermería (CU-E04)

### Motivo

Último paso del flujo de enfermería de la memoria (RF4, CU-E04, RN4). Desde la ficha rápida, en una
cita pendiente de hoy, la enfermera anota pruebas preliminares o actuaciones. El médico las verá
después en su agenda de escritorio.

### Comportamiento

- Se abre con el botón «Añadir observaciones» o «Editar observaciones» de cada cita pendiente de la
  ficha. La cita viaja en el Intent (`CitaDTO` es `Serializable`), así que no hay que pedirla otra vez.
- La cabecera muestra un resumen de la cita: paciente, alergias en rojo si tiene, hora, médico y motivo.
- El cuadro de texto viene relleno con las observaciones que ya tenga la cita. Tiene un contador
  hasta 2000 caracteres, el mismo límite que `MAX_OBSERVACIONES` del servidor.
- **Guardar** envía `GUARDAR_OBSERVACIONES` con `CitaDTO.conObservaciones(id, texto)` en una corrutina:
  - Si sale bien, muestra «Observaciones guardadas» y vuelve a la ficha, que recarga las citas en su
    `onResume()`.
  - Si el texto queda vacío, las borra y avisa con «Observaciones borradas».
  - Si el texto no ha cambiado, vuelve a la ficha sin llamar al servidor.
  - Mientras guarda, los botones y el texto se desactivan, para que no se pueda guardar dos veces.
- **Errores:**
  - Si el servidor lo rechaza (p. ej. el médico ya ha registrado la consulta y la cita ha dejado de
    estar pendiente), aparece un diálogo con su mensaje y el texto escrito se conserva.
  - Si se pierde la conexión, vuelve al login, como en el resto de pantallas.
- **Cancelar** o el botón «atrás» del sistema: si hay cambios sin guardar, pregunta antes de descartarlos.
- Al girar la pantalla no se pierde lo que se estaba escribiendo.
- Los márgenes se suman a las barras del sistema y al teclado (`adjustResize`), así que los botones
  quedan visibles por encima del teclado.

### Ficheros (Android, `src/EnfermeriaMedManager`)

- `app/src/main/java/.../ObservacionesActivity.kt` (nuevo): la pantalla 4.
- `app/src/main/res/layout/activity_observaciones.xml` (nuevo).
- `ClienteSocket.kt`: nuevo método `guardarObservaciones(idCita, texto)`.
- `FichaActivity.kt`: `abrirObservaciones()` abre la nueva pantalla en lugar del aviso «(próximamente)».
- `res/values/strings.xml`: textos de la pantalla de observaciones.
- `AndroidManifest.xml`: registro de `.ObservacionesActivity` con `adjustResize`.

El servidor no cambia: la acción ya existía y se revisó en la entrada «Revisión de las acciones de
enfermería».

### Comprobaciones realizadas

- `gradlew assembleDebug`: compila sin errores.
- Para tener una cita de hoy se creó la cita de prueba **id 7** (Ana Pérez, 25/11/2026 14:00,
  «Revisión de tensión»). Se creó a través del propio servidor, como `recepcion1`, con un cliente Java
  de prueba que no forma parte del repositorio.
- Prueba en el emulador (Pixel 6a), entrando como `enfermero1`:
  - La ficha de Ana muestra por fin las tarjetas de citas. Además de la id 7, sale otra pendiente a
    las 12:15 con motivo «prueba», que ya existía y no se ha tocado.
  - «Añadir observaciones» abre la pantalla con el resumen y la alergia en rojo. Con el teclado
    abierto, Cancelar y Guardar siguen visibles.
  - Se guardó «TA 130/85. Pulso 72. Glucemia 98.». El servidor registró «Observaciones guardadas en
    la cita 7 por enfermero1», y la ficha volvió con el texto y el botón «Editar observaciones».
  - Editar, escribir y pulsar «atrás» muestra el diálogo «¿Descartar los cambios?». Al pulsar
    Descartar vuelve a la ficha sin guardar.
  - Al vaciar el texto y guardar, la cita vuelve a «Sin observaciones de enfermería».
- Fallo encontrado y corregido durante la prueba: el contador de caracteres no se veía, porque el
  `EditText` ocupaba todo el alto del recuadro. Ahora tiene entre 6 y 10 líneas, y un `Space`
  empuja los botones abajo. Comprobado: muestra «33/2000».

### Cómo probarlo

1. Arranca el servidor y entra en la app como `enfermero1` / `Enfer123!`.
2. Busca «Ana» y abre su ficha. En «Citas de hoy» debe aparecer una cita pendiente.
3. Pulsa «Añadir observaciones», escribe por ejemplo «TA 130/85. Pulso 72.» y pulsa Guardar.
   - Debe volver a la ficha, con la tarjeta mostrando las observaciones y el botón «Editar observaciones».
4. Vuelve a entrar, cambia el texto y pulsa «atrás»: debe preguntar si descartas los cambios.
5. Borra todo el texto y guarda: la cita debe volver a «Sin observaciones de enfermería».

### Pendiente relacionado

- Comprobar en la app de escritorio, entrando como el médico de la cita, que ve las observaciones al
  registrar la consulta.
- La cita de prueba id 7 se puede cancelar desde recepción cuando ya no haga falta.

---

## 25/11/2026 · App Android: pantalla 3, ficha rápida del paciente (CU-E03)

### Motivo

Tercer paso del flujo de la memoria (RF4, CU-E03): al tocar un paciente en el buscador se ve su
identidad, sus alergias y su visita de hoy, desde la que se podrán añadir observaciones (pantalla 4).

### Comportamiento

- **Identidad:** nombre completo, DNI, fecha de nacimiento con la edad y teléfono («—» si no tiene).
  Llega en el `PacienteDTO` del buscador, que viaja en el `Intent` (es `Serializable`), así que no se
  hace otra petición.
- **Alergias:** recuadro con borde y fondo rojos si tiene alguna. Si no, «Sin alergias conocidas» sin destacar.
  El texto rojo oscuro sobre fondo rosa se lee bien también en modo oscuro.
- **Citas de hoy:** se piden con `CITAS_PACIENTE_HOY` (corrutina, igual que el buscador). Cada cita es
  una tarjeta con «hora · Dr/a. médico», el estado en color (pendiente azul, realizada verde,
  cancelada rojo), el motivo y las observaciones de enfermería. Sin citas: «No tiene citas hoy».
- Las citas se piden en `onResume()`, no en `onCreate()`. Así, al volver de la pantalla de
  observaciones, la ficha se actualiza sola.
- En las citas **PENDIENTES** aparece el botón «Añadir observaciones», o «Editar observaciones» si ya
  tiene. De momento muestra un `Toast` «(próximamente)»: ahí irá la pantalla 4 con `GUARDAR_OBSERVACIONES`.
  En las realizadas o canceladas no aparece, porque el servidor las rechazaría.
- «← Volver» o el botón atrás → regresa al buscador con los resultados intactos.
- Mismo tratamiento de errores que el buscador. Al perder la conexión se vuelve al login **borrando
  toda la pila de pantallas** (`FLAG_ACTIVITY_CLEAR_TASK`), para que «atrás» no lleve a pantallas sin sesión.

### Ficheros (Android, `src/EnfermeriaMedManager`)

- `FichaActivity.kt` (**nuevo**): la pantalla. Las citas se pintan inflando `item_cita.xml` dentro de un
  `LinearLayout`: son pocas al día, así que no hace falta `RecyclerView`.
- `Utilidades.kt` (**nuevo**): `edad(fecha)` y `Activity.volverAlLogin()`, compartidas por el buscador y la ficha.
- `res/layout/activity_ficha.xml` e `item_cita.xml` (**nuevos**).
- `ClienteSocket.kt`: nuevo `citasPacienteHoy(idPaciente): List<CitaDTO>`.
- `BuscadorActivity.kt`:
  - Al tocar un paciente abre `FichaActivity`.
  - Usa `volverAlLogin()` de `Utilidades`.
  - **Corrección:** el listener de *insets* sustituía el `padding="16dp"` del XML y el contenido quedaba
    pegado a los bordes. Ahora suma el margen de las barras del sistema a esos 16dp.
- `PacienteAdapter.kt`: usa `edad()` de `Utilidades`.
- `res/values/strings.xml` y `colors.xml`: textos de la ficha y colores `alerta_fondo`, `pendiente` y `realizada`.
- `AndroidManifest.xml`: se registra `FichaActivity`.

**Servidor:** sin cambios.

### Comprobaciones realizadas

Probado **en el emulador** con el servidor en marcha y los datos de `03_datos_prueba_medmanager.sql`:

- Login con `enfermero1` → buscador «Hola, Jordi».
- Buscar «Ana» y «Perez» (sin tilde) → sale «Pérez López, Ana · DNI 55555555E · 41 años», con
  «⚠ Alergias: Penicilina» en rojo. La búsqueda ignora las tildes por la *collation* de MySQL.
- Tocar a Ana → ficha con identidad, recuadro rojo de alergias y «No tiene citas hoy». Es correcto:
  Ana no tiene ninguna cita hoy, así que `CITAS_PACIENTE_HOY` responde con la lista vacía.
- Márgenes del buscador corregidos (comprobado con captura).
- **Sin probar:** las tarjetas de citas, porque hoy no había ninguna para pintarlas.

### Cómo probarlo

1. Desde el escritorio (recepción), crear una cita **para hoy** a Ana Pérez.
2. En la app: buscar «Ana» → tocarla → aparece la tarjeta de la cita, «Pendiente» en azul,
   con el motivo, «Sin observaciones de enfermería» y el botón «Añadir observaciones».
3. Cancelar esa cita desde recepción, volver atrás y entrar de nuevo en la ficha (o salir y volver a la app):
   aparece «Cancelada» en rojo y sin botón.
4. Pedro Sanz (sin alergias) → recuadro neutro con «Sin alergias conocidas».

### Pendiente relacionado

- `MainActivity` (login) tiene el mismo problema de márgenes que tenía el buscador: su `padding="32dp"`
  se pierde con el listener de *insets*. No se ha tocado.

---

## 25/11/2026 · App Android: pantalla 2, buscador de pacientes (CU-E02)

### Motivo

Segundo paso del flujo de la memoria (RF4 y wireframes): tras el login, la enfermera busca al paciente
por DNI, nombre o apellidos y, al tocarlo, se abrirá su ficha rápida (pantalla 3, todavía pendiente).

### Comportamiento

- El login abre `BuscadorActivity` si el usuario es ENFERMERO y cierra la pantalla de login
  (`finish()`), así el botón «atrás» no vuelve a ella.
- Cabecera con «Hola, <nombre>» y botón **Cerrar sesión**, que cierra el socket (y con él la sesión
  del servidor) y vuelve al login.
- Campo de búsqueda + botón **Buscar** (también la tecla «buscar» del teclado). Se exigen al menos
  **2 caracteres**: el servidor con texto vacío devuelve los 50 primeros pacientes y no tiene sentido
  listarlos todos.
- Cada resultado es una tarjeta con «Apellidos, Nombre», «DNI · edad» y las **alergias en rojo**
  («Sin alergias conocidas» si no tiene). El servidor ya manda las alergias a enfermería (RN3) y solo
  devuelve pacientes activos.
- Mientras busca se ve un `ProgressBar` y el botón se desactiva. Sin resultados aparece
  «No hay pacientes que coincidan con «…»».
- Si se gira la pantalla, los resultados se conservan (`onSaveInstanceState`; `PacienteDTO` es `Serializable`).
- Errores:
  - El servidor responde `ERROR` → `Toast` con su mensaje.
  - Fallo de red (`IOException`) → aviso y vuelta al login, porque al caer la conexión el servidor
    pierde la sesión.
  - Si Android recrea la pantalla sin sesión (`usuarioActual == null`), se vuelve al login.
- Tocar un paciente muestra de momento un `Toast` «(próximamente)». Ahí irá la ficha rápida con
  `CITAS_PACIENTE_HOY`.

### Red con corrutinas

La memoria (§4.2) indica corrutinas para la red. El buscador usa `lifecycleScope.launch` y
`withContext(Dispatchers.IO) { ClienteSocket.buscarPacientes(texto) }`: la petición se hace fuera del
hilo principal y el resultado se pinta al volver a él, sin `runOnUiThread`. Si se cierra la pantalla,
la corrutina se cancela sola. El login (`MainActivity`) sigue usando `Thread` + `runOnUiThread`, sin tocar.

### Ficheros

**Android (`src/EnfermeriaMedManager`)**

- `gradle/libs.versions.toml` y `app/build.gradle.kts`: dependencias `androidx.lifecycle:lifecycle-runtime-ktx:2.10.0`
  (`lifecycleScope`) y `androidx.recyclerview:recyclerview:1.2.1`. Son las versiones que ya resolvía
  Gradle de forma transitiva, así que no cambia nada más.
- `ClienteSocket.kt`: nuevo `buscarPacientes(texto): List<PacienteDTO>` (acción `BUSCAR_PACIENTES`).
- `BuscadorActivity.kt` (**nuevo**): la pantalla.
- `PacienteAdapter.kt` (**nuevo**): adaptador del `RecyclerView`; calcula la edad con `Period.between`.
- `res/layout/activity_buscador.xml` y `res/layout/item_paciente.xml` (**nuevos**).
- `res/values/strings.xml`: textos de la pantalla. `res/values/colors.xml`: color `alerta` para las alergias.
- `AndroidManifest.xml`: se registra `BuscadorActivity` (`exported="false"`, `adjustResize`).
- `MainActivity.kt`: tras un login de enfermería, `startActivity(BuscadorActivity)` + `finish()`.

**Servidor:** sin cambios.

### Comprobaciones realizadas

- `gradlew :app:assembleDebug` → BUILD SUCCESSFUL.
- **No se ha probado en ejecución**: el servidor MedManager no estaba arrancado (MySQL sí).

### Cómo probarlo

1. Arrancar el servidor en Eclipse y ejecutar la app en el emulador.
2. Iniciar sesión con un usuario ENFERMERO → aparece «Hola, <nombre>» y el buscador.
3. Buscar 1 letra → error «Escribe al menos 2 caracteres».
4. Buscar por parte del apellido, por DNI y por «nombre apellido» → salen las tarjetas; los pacientes
   con alergias las muestran en rojo.
5. Buscar algo inexistente → «No hay pacientes que coincidan…».
6. Girar el emulador → los resultados siguen ahí.
7. Parar el servidor y buscar → aviso de conexión perdida y vuelta al login.
8. «Cerrar sesión» → vuelve al login; «atrás» desde el buscador sale de la app.

---

## 25/11/2026 · Revisión de las acciones de enfermería y arreglo de la compilación Android

### Contexto

Antes de empezar la segunda pantalla de Android (buscador de pacientes) se ha contrastado el flujo
con la memoria. RF4, CU-E02 a CU-E04 y los wireframes describen: **buscar paciente por DNI o nombre →
ficha rápida (identidad, alergias y visita pendiente) → añadir observaciones**. No es un listado con
todas las citas del día. Las acciones del 24/11 encajan con ese flujo:

| Paso de la app | Acción |
|---|---|
| Buscador | `BUSCAR_PACIENTES` (ya existía; para ENFERMERO devuelve las alergias) |
| Ficha rápida: visita pendiente | `CITAS_PACIENTE_HOY` |
| Observaciones | `GUARDAR_OBSERVACIONES` |

### Cambios en el código

Ninguno: la implementación del servidor del 24/11 se ha revisado y es correcta.

- `ManejadorCliente.guardarObservaciones()` convierte a DTO la `Cita` después de cerrar la sesión de
  Hibernate. No da `LazyInitializationException` porque `Cita.paciente` y `Cita.trabajador` son
  `@ManyToOne` sin `fetch`, es decir, `EAGER` por defecto. Si algún día se cambian a `LAZY`, este
  método y `cancelar`/`reprogramar` tendrán que hacer `JOIN FETCH`.

### Arreglo de entorno (no afecta al repositorio)

- La app Android no compilaba: todas las tareas fallaban con «Unable to delete directory … app\build\…».
  Las carpetas de `src/EnfermeriaMedManager` tenían el atributo de **solo lectura** de Windows y Gradle
  no podía borrar las salidas antiguas. Se quitó con
  `attrib -R "src\EnfermeriaMedManager\*" /S /D` y se borró `app/build`, que es contenido generado.
- El emulador Pixel 6a se quedaba en «Waiting for all target devices to come online» (`adb` lo veía
  `offline`). Se solucionó con un arranque en frío: *Device Manager → Cold Boot Now*.

### Comprobaciones realizadas

- El servidor se compila sin errores con `javac`, usando las dependencias de `~/.m2`.
- El paquete `comun` sigue siendo idéntico en los tres proyectos (`diff`/`cmp`).
- La app Android se compila con `gradlew :app:assembleDebug` y se ejecuta en el emulador.
- **Sigue pendiente** probar en ejecución `CITAS_PACIENTE_HOY` y `GUARDAR_OBSERVACIONES`
  (pasos de «Cómo probarlo» de la entrada del 24/11).

### Pendiente relacionado

- Memoria, apartado de funcionalidades → «Aplicación Android»: dice «solo accederán los **médicos**».
  Debe decir **enfermeros**.
- Memoria, tabla de acciones del protocolo: sustituir el `[PENDIENTE: añadir las acciones de enfermería…]`
  por las filas de `CITAS_PACIENTE_HOY` y `GUARDAR_OBSERVACIONES`.

---

## 24/11/2026 · Acciones de enfermería en el servidor y paquete `comun` unificado

### Motivo

La app Android de enfermería solo tenía el login. El servidor no ofrecía nada al rol `ENFERMERO`
salvo `BUSCAR_PACIENTES`, y la clase `Acciones.java` de Android no coincidía con la del servidor:

- Definía `GUARDAR_OBSERVACIONES`, que el servidor no conocía. Si se enviaba, el guardián de permisos
  la rechazaba con «No tienes permiso para esta acción» (se deniega por defecto).
- Le faltaban `GUARDAR_INFORME`, `CREAR_CITA`, `CANCELAR_CITA`, `REPROGRAMAR_CITA`,
  `GUARDAR_PACIENTE` y `BAJA_PACIENTE`.

Con este cambio quedan cubiertos el requisito de la ficha rápida y la regla RN4
(enfermería anota observaciones en la cita).

### Nuevas acciones del protocolo

| Acción | Rol | Payload que envía el cliente | Respuesta `OK` |
|---|---|---|---|
| `CITAS_PACIENTE_HOY` | ENFERMERO | `Integer` id del paciente | `List<CitaDTO>` con las citas de hoy del paciente (de cualquier médico y en cualquier estado), ordenadas por hora y con alergias y observaciones (RN3) |
| `GUARDAR_OBSERVACIONES` | ENFERMERO | `CitaDTO` con el id de la cita y `observacionesEnfermeria`. Se crea con `CitaDTO.conObservaciones(idCita, texto)` | `CitaDTO` actualizada |

**Cómo se monta la ficha rápida en Android:** los datos personales y las alergias ya llegan en el
`PacienteDTO` de `BUSCAR_PACIENTES`, porque el servidor solo oculta las alergias a recepción.
La ficha solo tiene que pedir además `CITAS_PACIENTE_HOY`. No ha hecho falta crear un DTO nuevo.

### Reglas de negocio de `GUARDAR_OBSERVACIONES`

Las validaciones las hace el servidor; no se fía del cliente.

1. La cita debe existir. Si no: «La cita no existe».
2. Debe estar **PENDIENTE**. Una cita REALIZADA o CANCELADA ya no se modifica:
   «Solo se pueden anotar observaciones en citas pendientes (esta está …)».
3. Debe ser **de hoy**, porque enfermería atiende al paciente antes de que pase a consulta:
   «Solo se pueden anotar observaciones en las citas de hoy».
4. El texto puede tener como máximo **2000 caracteres**. La columna es `TEXT`, pero se limita para evitar abusos.
5. Un texto vacío **borra** las observaciones anteriores, porque se guarda `NULL`.

El médico ve las observaciones en su agenda de escritorio, en la columna «Observaciones de enfermería»
de `DashboardMedico`, que ya existía. Recepción no las ve (RN3, en `Conversor.aDTO(cita, false)`).

### Ficheros modificados

**Servidor (`src/ServidorMedManager`)**

- `comun/Acciones.java`: nuevas constantes `CITAS_PACIENTE_HOY` y `GUARDAR_OBSERVACIONES`, en una
  sección «Enfermería». El comentario de la clase indica ahora que es idéntica en los **tres** proyectos.
- `comun/CitaDTO.java`: nuevo método de fábrica `conObservaciones(idCita, observaciones)` y comentario
  de `alergiasPaciente` corregido (también las ve enfermería). No cambia ningún campo, así que el
  `serialVersionUID = 1L` sigue siendo válido.
- `Permisos.java`: las dos acciones nuevas se permiten solo al rol `ENFERMERO`. El comentario de
  `BUSCAR_PACIENTES` deja claro que es común a los tres roles.
- `comun/Acciones.java` (aclaración): `BUSCAR_PACIENTES` pasa a la sección «Común a todos los roles
  (recepción, médico y enfermería)». Antes decía «médico y recepción», aunque `Permisos` ya incluía
  a `ENFERMERO`. Solo cambia el comentario, no el comportamiento.
- `ManejadorCliente.java`: dos `case` nuevos en el `switch` y los métodos `citasPacienteHoy()` y
  `guardarObservaciones()`, más la constante `MAX_OBSERVACIONES = 2000`.
- `dao/CitaDAO.java`:
  - `citasDePacienteEnDia(idPaciente, dia)`: consulta HQL con `JOIN FETCH` de paciente y médico,
    para que `Conversor` no provoque cargas perezosas fuera de la sesión.
  - `guardarObservaciones(idCita, texto)`: transacción con las reglas 1 a 3 y *rollback* antes de
    cerrar la sesión, igual que el resto de métodos del DAO.

**Escritorio (`src/EscritorioMedManager`)**

- `comun/*.java`: copiado desde el servidor. Los 8 ficheros son idénticos.

**Android (`src/EnfermeriaMedManager`)**

- `comun/Acciones.java`: sustituido por el del servidor. Desaparece la versión divergente.
- `comun/CitaDTO.java` y `comun/PacienteDTO.java`: **nuevos**, copiados del servidor. Hacen falta para
  la búsqueda, la ficha y las observaciones. Mismo paquete `com.medmanager.comun` y mismo
  `serialVersionUID`, porque la deserialización de Java lo exige.
- `comun/LoginDTO.java`, `PaqueteRed.java` y `TrabajadorDTO.java`: copiados de nuevo. Solo cambian
  los saltos de línea o el salto final.
- Android no necesita `HistorialDTO` ni `InformeDTO`, porque enfermería no usa esas acciones.

**Documentación**

- `docs/Diagramas/Recorridos/recorrido_android.puml`: la nota ya no dice que las acciones no existen.
  El paso 3 usa `CITAS_PACIENTE_HOY` y el paso 4 usa `CitaDTO.conObservaciones(...)`.
  ⚠️ El enlace de Kroki del `README.md` de esa carpeta todavía genera el diagrama antiguo.
  Hay que regenerarlo pegando el `.puml` en un editor de PlantUML.

### Comprobaciones realizadas

- El servidor y el escritorio se compilan sin errores con `javac`, usando las dependencias de `~/.m2`.
- El paquete `comun` del escritorio es idéntico al del servidor (`diff -r`), y los 6 ficheros de
  Android son idénticos byte a byte.
- **No se ha probado en ejecución** (servidor + MySQL + cliente) ni se ha compilado la app Android con Gradle.

### Cómo probarlo

1. Reiniciar el servidor en Eclipse. **Es obligatorio:** el servidor en marcha no conoce las acciones nuevas.
2. Crear desde recepción una cita PENDIENTE para **hoy**.
3. Con un cliente de prueba (por ejemplo, `ClientePrueba`) iniciar sesión como enfermero y enviar:
   - `PaqueteRed(CITAS_PACIENTE_HOY, idPaciente)` → debe devolver la cita.
   - `PaqueteRed(GUARDAR_OBSERVACIONES, CitaDTO.conObservaciones(idCita, "TA 12/8, 36,5 ºC"))` → `OK`.
4. Entrar en el escritorio como el médico de esa cita: la columna «Observaciones de enfermería» muestra el texto.
5. Casos de error que conviene capturar para el §8 de la memoria:
   - La misma petición iniciando sesión como recepcionista → «No tienes permiso para esta acción».
   - Observaciones en una cita de otro día → «Solo se pueden anotar observaciones en las citas de hoy».
   - Observaciones en una cita cancelada → «Solo se pueden anotar observaciones en citas pendientes…».

### Pendiente relacionado

- `database/Files SQL/Mod Observaciones.sql` añade una columna `observaciones` que **no se usa**:
  la entidad `Cita` usa `observaciones_enfermeria`, que ya está en `CrearTablasDefinitivo.sql`.
  Hay que decidir si se borra ese script. Si ya se ejecutó, conviene `ALTER TABLE cita DROP COLUMN observaciones;`.
- `ClienteSocket.kt` declara el paquete `...enfermeriamedmanager.red` pero está en la carpeta
  `enfermeriamedmanager/`. Conviene moverlo a `red/`.
- Siguiente paso: las pantallas de Android (búsqueda, ficha rápida y observaciones con corrutinas).
