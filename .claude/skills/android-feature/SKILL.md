---
name: android-feature
description: >
  Genera el scaffold completo de una nueva feature Android siguiendo
  Clean Architecture + MVVM + Hilt. Úsala cuando el usuario diga
  "crear feature", "nueva pantalla", "agregar módulo", o "implementar
  [funcionalidad] en Android". Genera las capas: domain, data,
  presentation y el test unitario del UseCase.
---

# Skill: Scaffold de Feature Android (Clean Architecture)

## Protocolo de Generación

Cuando se invoca esta skill, pide al usuario (si no lo especificó):
1. **Nombre de la feature** (ej: `CallRouting`, `CallerIdentification`)
2. **¿Qué hace?** (descripción en una oración)
3. **¿Necesita UI?** (pantalla nueva o solo lógica de servicio)

Luego genera los siguientes archivos en orden:

---

## Estructura a Generar

```
feature-[nombre]/
├── domain/
│   ├── model/[Nombre]Model.kt
│   ├── repository/I[Nombre]Repository.kt
│   └── usecase/[Accion][Nombre]UseCase.kt
├── data/
│   ├── repository/[Nombre]RepositoryImpl.kt
│   └── datasource/[Nombre]DataSource.kt
├── presentation/
│   ├── [Nombre]ViewModel.kt
│   ├── [Nombre]Screen.kt          ← Solo si tiene UI
│   └── [Nombre]UiState.kt
├── di/
│   └── [Nombre]Module.kt
└── test/
    └── [Accion][Nombre]UseCaseTest.kt
```

---

## Templates

### 1. Model (domain/model)
```kotlin
data class [Nombre]Model(
    val id: String,
    // propiedades específicas de la feature
)
```

### 2. Repository Interface (domain/repository)
```kotlin
interface I[Nombre]Repository {
    suspend fun get[Nombre](id: String): Result<[Nombre]Model>
    fun observe[Nombre](): Flow<[Nombre]Model>
}
```

### 3. UseCase (domain/usecase)
```kotlin
class [Accion][Nombre]UseCase @Inject constructor(
    private val repository: I[Nombre]Repository
) {
    suspend operator fun invoke(param: String): Result<[Nombre]Model> {
        return repository.get[Nombre](param)
    }
}
```

### 4. UiState (presentation)
```kotlin
data class [Nombre]UiState(
    val isLoading: Boolean = false,
    val data: [Nombre]Model? = null,
    val error: String? = null
)
```

### 5. ViewModel (presentation)
```kotlin
@HiltViewModel
class [Nombre]ViewModel @Inject constructor(
    private val use[Nombre]UseCase: [Accion][Nombre]UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow([Nombre]UiState())
    val uiState: StateFlow<[Nombre]UiState> = _uiState.asStateFlow()

    fun load[Nombre](id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            use[Nombre]UseCase(id)
                .onSuccess { data ->
                    _uiState.update { it.copy(isLoading = false, data = data) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}
```

### 6. Hilt Module (di)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class [Nombre]Module {

    @Binds
    @Singleton
    abstract fun bind[Nombre]Repository(
        impl: [Nombre]RepositoryImpl
    ): I[Nombre]Repository
}
```

### 7. Test del UseCase
```kotlin
class [Accion][Nombre]UseCaseTest {

    private val repository: I[Nombre]Repository = mockk()
    private val useCase = [Accion][Nombre]UseCase(repository)

    @Test
    fun `when repository returns data, useCase returns success`() = runTest {
        val expected = [Nombre]Model(id = "test-id")
        coEvery { repository.get[Nombre](any()) } returns Result.success(expected)

        val result = useCase("test-id")

        assert(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `when repository throws, useCase returns failure`() = runTest {
        coEvery { repository.get[Nombre](any()) } returns Result.failure(Exception("Error"))

        val result = useCase("test-id")

        assert(result.isFailure)
    }
}
```

---

## Instrucciones Finales

1. Reemplaza todos los `[Nombre]` y `[Accion]` con los valores reales
2. Adapta los campos del `Model` a los datos reales de la feature
3. Si la feature no tiene UI, omite `[Nombre]Screen.kt` y `[Nombre]UiState.kt`
4. Siempre genera el test del UseCase — es el contrato del comportamiento
5. Explica brevemente cada capa generada usando una analogía del mundo real
