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

    // --- AÑADIDA NUEVA RUTA ---
    object MyReservations : Route("my_reservations")
}