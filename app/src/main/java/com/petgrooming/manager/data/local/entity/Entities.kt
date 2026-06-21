package com.petgrooming.manager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "owners")
data class OwnerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobileNumber: String,
    val email: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val petType: PetType = PetType.DOG,
    val breed: String,
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null,
    val weight: Float? = null,
    val color: String? = null,
    val allergies: String? = null,
    val medications: String? = null,
    val behaviorNotes: String? = null,
    val notes: String? = null,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PetType {
    DOG, CAT, OTHER
}

enum class Gender {
    MALE, FEMALE
}

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId"), Index("appointmentDate")]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val appointmentDate: LocalDate,
    val appointmentTime: LocalTime,
    val serviceType: ServiceType,
    val status: BookingStatus = BookingStatus.SCHEDULED,
    val price: Double? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val groomerName: String? = null,
    val notes: String? = null,
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ServiceType {
    BATH,
    BATH_AND_DRY,
    FULL_GROOM,
    NAIL_TRIM,
    EAR_CLEANING,
    CUSTOM
}

enum class BookingStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

enum class PaymentStatus {
    UNPAID,
    PAID,
    REFUNDED
}

@Entity(tableName = "service_prices")
data class ServicePriceEntity(
    @PrimaryKey
    val serviceType: ServiceType,
    val price: Double = 0.0
)

@Entity(
    tableName = "rebooking_reminders",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId"), Index("dueDate")]
)
data class RebookingReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val lastGroomDate: LocalDate,
    val dueDate: LocalDate,
    val intervalWeeks: Int = 8,
    val reminder7DaySent: Boolean = false,
    val reminderDueDateSent: Boolean = false,
    val reminder14DayOverdueSent: Boolean = false
)

@Entity(
    tableName = "custom_breeds",
    indices = [Index(value = ["petType", "breedName"], unique = true)]
)
data class CustomBreedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petType: PetType,
    val breedName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "custom_colors",
    indices = [Index(value = ["colorName"], unique = true)]
)
data class CustomColorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val colorName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "custom_list_items",
    indices = [Index(value = ["category", "value"], unique = true)]
)
data class CustomListItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val value: String,
    val createdAt: Long = System.currentTimeMillis()
)
