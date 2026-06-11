package com.example.focusflow.data.model

data class RutinaSorpresa(
    val codigo: String,
    val especialista: String,
    val titulo: String,
    val descripcion: String,
)

object RutinaSorpresaProvider {
    val rutinas = listOf(
        RutinaSorpresa(
            "FOCUS_01", "Dra. Luna", "Rutina de enfoque suave",
            "1. Respira profundo durante 2 minutos.\n2. Ordena tu escritorio por 3 minutos.\n3. Estudia una sola tarea durante 15 minutos.\n4. Descansa 5 minutos sin celular.\n5. Marca tu avance en la app.",
        ),
        RutinaSorpresa(
            "FOCUS_02", "Dr. Leo", "Rutina anti-distracción",
            "1. Elige una tarea pequeña.\n2. Guarda objetos que te distraigan.\n3. Activa un temporizador de 20 minutos.\n4. Trabaja solo en esa tarea.\n5. Al terminar, date una pausa corta.",
        ),
        RutinaSorpresa(
            "FOCUS_03", "Dra. Sol", "Rutina de calma y organización",
            "1. Toma agua.\n2. Respira lentamente 5 veces.\n3. Escribe 3 cosas que debes hacer.\n4. Empieza por la tarea más fácil.\n5. Felicítate al terminar.",
        ),
        RutinaSorpresa(
            "FOCUS_04", "Dr. Mateo", "Rutina Pomodoro para TDAH",
            "1. Estudia 10 minutos.\n2. Descansa 3 minutos.\n3. Repite el ciclo 2 veces.\n4. Evita cambiar de tarea.\n5. Registra cómo te sentiste.",
        ),
    )

    fun getRutinaByCodigo(codigo: String): RutinaSugerida? {
        val sorpresa = rutinas.find { it.codigo == codigo } ?: return null
        
        // Convertimos las líneas de la descripción en tareas individuales
        val tareas = sorpresa.descripcion.lines()
            .filter { it.isNotBlank() }
            .map { linea ->
                TareaSugerida(
                    titulo = linea.substringAfter(". ").trim(),
                    descripcion = "Paso de la rutina ${sorpresa.titulo}",
                    duracionEstimada = "Variable"
                )
            }

        return RutinaSugerida(
            nombre = sorpresa.titulo,
            descripcion = "Especialista: ${sorpresa.especialista}. ${sorpresa.descripcion}",
            tareas = tareas
        )
    }
}
