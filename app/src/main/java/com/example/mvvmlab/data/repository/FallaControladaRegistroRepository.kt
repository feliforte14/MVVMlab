package com.example.mvvmlab.data.repository

import com.example.mvvmlab.domain.model.Registro
import com.example.mvvmlab.domain.repository.RegistroRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow

// --- Falla CONTROLADA (Etapa 4: "provocar una falla del Repository") -----
// Envuelve al Repository real: lee normalmente pero toda escritura falla.
// Se activa en ContenedorApp (SIMULAR_FALLA_EN_REGISTROS). No se tocan
// permisos ni archivos del dispositivo.
class FallaControladaRegistroRepository(
    private val real: RegistroRepository
) : RegistroRepository {
    override fun observar(busqueda: String): Flow<List<Registro>> = real.observar(busqueda)
    override suspend fun obtener(id: String): Registro? = real.obtener(id)
    override suspend fun guardar(registro: Registro) = throw IOException("Falla simulada al guardar")
    override suspend fun eliminar(id: String) = throw IOException("Falla simulada al eliminar")
    override suspend fun cambiarFavorito(id: String, favorito: Boolean) =
        throw IOException("Falla simulada al marcar favorito")
}
