package com.example.familyhealth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlin.random.Random

data class HealthTip(
    val title: String,
    val description: String,
    val category: String
)

data class UserHealthProfile(
    val age: Int = 35,
    val sleepsHours: Int = 6,
    val isSmoker: Boolean = false,
    val weeklyExerciseMinutes: Int = 60
)

class HealthTipsViewModel : ViewModel() {


    private val _userProfile = mutableStateOf(UserHealthProfile())
    val userProfile: State<UserHealthProfile> get() = _userProfile


    private val _generalTips = mutableStateOf(
        listOf(
            HealthTip(
                title = "Hidratación",
                description = "Bebe entre 1.5 y 2 litros de agua al día, evitando bebidas azucaradas.",
                category = "Nutrición"
            ),
            HealthTip(
                title = "Actividad física",
                description = "Intenta realizar al menos 30 minutos de ejercicio moderado la mayoría de los días.",
                category = "Actividad física"
            ),
            HealthTip(
                title = "Sueño reparador",
                description = "Procura dormir entre 7 y 9 horas cada noche y mantén horarios regulares.",
                category = "Sueño"
            ),
            HealthTip(
                title = "Alimentación equilibrada",
                description = "Incluye frutas, verduras, legumbres y cereales integrales en tu dieta diaria.",
                category = "Nutrición"
            ),
            HealthTip(
                title = "Gestión del estrés",
                description = "Practica técnicas de relajación como respiración profunda 5 minutos al día.",
                category = "Salud mental"
            ),
            HealthTip(
                title = "Sedentarismo",
                description = "Si pasas muchas horas sentado, levántate y camina 5 minutos cada hora.",
                category = "Actividad física"
            )
        )
    )
    val generalTips: State<List<HealthTip>> get() = _generalTips


    private val _personalizedTips = mutableStateOf<List<HealthTip>>(emptyList())
    val personalizedTips: State<List<HealthTip>> get() = _personalizedTips


    private val _dailyTip = mutableStateOf<HealthTip?>(null)
    val dailyTip: State<HealthTip?> get() = _dailyTip

    init {
        generatePersonalizedTips()
        pickDailyTip()
    }

    private fun generatePersonalizedTips() {
        val profile = _userProfile.value
        val tips = mutableListOf<HealthTip>()

        if (profile.sleepsHours < 7) {
            tips.add(
                HealthTip(
                    title = "Mejora tu descanso",
                    description = "Duermes menos de 7 horas. Intenta adelantar la hora de irte a la cama 30 minutos.",
                    category = "Sueño"
                )
            )
        }

        if (profile.weeklyExerciseMinutes < 150) {
            tips.add(
                HealthTip(
                    title = "Muévete un poco más",
                    description = "Realizas menos de 150 minutos de ejercicio a la semana. Empieza con paseos de 10–15 minutos diarios.",
                    category = "Actividad física"
                )
            )
        }

        if (profile.isSmoker) {
            tips.add(
                HealthTip(
                    title = "Reducción del tabaco",
                    description = "Si fumas, intenta reducir el número de cigarrillos al día y busca apoyo profesional.",
                    category = "Hábitos tóxicos"
                )
            )
        }

        if (profile.age > 40) {
            tips.add(
                HealthTip(
                    title = "Revisiones médicas",
                    description = "A partir de los 40 años se recomiendan chequeos médicos periódicos con tu médico de familia.",
                    category = "Prevención"
                )
            )
        }

        _personalizedTips.value = tips
    }

    private fun pickDailyTip() {
        val allTips = _generalTips.value
        if (allTips.isNotEmpty()) {
            _dailyTip.value = allTips[Random.nextInt(allTips.size)]
        }
    }
}
