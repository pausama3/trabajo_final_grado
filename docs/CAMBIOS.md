# Registro de cambios · MedManager

Cada entrada explica qué ha cambiado, por qué y cómo se prueba. Las más recientes van arriba.

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
