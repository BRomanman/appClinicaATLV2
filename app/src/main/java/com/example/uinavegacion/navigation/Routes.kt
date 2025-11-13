package com.example.uinavegacion.navigation

// Definimos las rutas selladas
sealed class Route(val path: String) {
    object Home : Route("home")
    object Login : Route("login")
    object Register : Route("register")

    // Rutas de la Clínica
    object PatientMenu : Route("patient_menu")
    object DoctorMenu : Route("doctor_menu")
    object AdminMenu : Route("admin_menu")
    object BookAppointment : Route("book_appointment")
    object MyReservations : Route("my_reservations")
    object PatientProfile : Route("patient_profile")
    object DoctorProfile : Route("doctor_profile/{doctorId}") {
        fun createRoute(id: Long) = "doctor_profile/$id"
    }

    // --- AÑADIDA NUEVA RUTA DE ADMIN ---
    object AdminManageSpecialties : Route("admin_manage_specialties")
    // (Añadiremos la ruta para editar doctores/estadísticas después)
}