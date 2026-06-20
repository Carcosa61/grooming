package com.petgrooming.manager.data.local

import androidx.room.TypeConverter
import com.petgrooming.manager.data.local.entity.BookingStatus
import com.petgrooming.manager.data.local.entity.Gender
import com.petgrooming.manager.data.local.entity.PetType
import com.petgrooming.manager.data.local.entity.ServiceType
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? = timeString?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromGender(gender: Gender?): String? = gender?.name

    @TypeConverter
    fun toGender(genderString: String?): Gender? = genderString?.let { Gender.valueOf(it) }

    @TypeConverter
    fun fromPetType(petType: PetType?): String? = petType?.name

    @TypeConverter
    fun toPetType(petTypeString: String?): PetType? = petTypeString?.let { PetType.valueOf(it) }

    @TypeConverter
    fun fromServiceType(serviceType: ServiceType?): String? = serviceType?.name

    @TypeConverter
    fun toServiceType(serviceTypeString: String?): ServiceType? = 
        serviceTypeString?.let { ServiceType.valueOf(it) }

    @TypeConverter
    fun fromBookingStatus(status: BookingStatus?): String? = status?.name

    @TypeConverter
    fun toBookingStatus(statusString: String?): BookingStatus? = 
        statusString?.let { BookingStatus.valueOf(it) }
}
