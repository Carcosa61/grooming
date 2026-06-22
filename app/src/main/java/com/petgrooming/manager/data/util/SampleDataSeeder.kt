package com.petgrooming.manager.data.util

import android.content.Context
import com.petgrooming.manager.data.local.entity.Gender
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.data.local.entity.PetType
import com.petgrooming.manager.domain.repository.OwnerRepository
import com.petgrooming.manager.domain.repository.PetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds a one-time set of Thai-language sample owners and pets. Existing owners
 * (and, via cascade, their pets) are cleared first. Guarded by a preference flag
 * so it only runs once per install.
 */
@Singleton
class SampleDataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ownerRepository: OwnerRepository,
    private val petRepository: PetRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun seedIfNeeded() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        scope.launch {
            // Overwrite any existing data (pets cascade-delete with their owner).
            ownerRepository.getAllOwners().first().forEach { ownerRepository.deleteOwner(it) }

            val ownerIds = SAMPLE_OWNERS.map { ownerRepository.insertOwner(it) }

            SAMPLE_PETS.forEach { (ownerIndex, pet) ->
                petRepository.insertPet(pet.copy(ownerId = ownerIds[ownerIndex]))
            }

            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "sample_data"
        private const val KEY_SEEDED = "sample_data_seeded_v1"

        private val SAMPLE_OWNERS = listOf(
            OwnerEntity(name = "สมชาย ใจดี", mobileNumber = "081-234-5678", lineId = "somchai.j"),
            OwnerEntity(name = "สมหญิง รักสุข", mobileNumber = "082-345-6789", lineId = "somying"),
            OwnerEntity(name = "ประเสริฐ ทองคำ", mobileNumber = "083-456-7890"),
            OwnerEntity(name = "มาลี ดอกไม้", mobileNumber = "084-567-8901", lineId = "malee_d"),
            OwnerEntity(name = "วิชัย แสงทอง", mobileNumber = "085-678-9012"),
            OwnerEntity(name = "นภา ฟ้าใส", mobileNumber = "086-789-0123", lineId = "napha"),
            OwnerEntity(name = "อนุชา พงษ์ไพร", mobileNumber = "087-890-1234"),
            OwnerEntity(name = "กนกวรรณ ศรีสุข", mobileNumber = "088-901-2345", lineId = "kanok"),
            OwnerEntity(name = "ธนวัฒน์ มั่งมี", mobileNumber = "089-012-3456"),
            OwnerEntity(name = "ปิยะดา ใจงาม", mobileNumber = "090-123-4567", lineId = "piyada"),
            OwnerEntity(name = "สุรชัย เกียรติยศ", mobileNumber = "091-234-5678"),
            OwnerEntity(name = "จันทร์เพ็ญ พระจันทร์", mobileNumber = "092-345-6789", lineId = "janpen"),
            OwnerEntity(name = "เอกพงษ์ รุ่งเรือง", mobileNumber = "093-456-7890"),
            OwnerEntity(name = "ศิริพร บุญมา", mobileNumber = "094-567-8901", lineId = "siriporn"),
            OwnerEntity(name = "ชาญชัย ชนะศึก", mobileNumber = "095-678-9012"),
            OwnerEntity(name = "พรทิพย์ เทพธิดา", mobileNumber = "096-789-0123", lineId = "pornthip"),
            OwnerEntity(name = "ณัฐพล วงศ์ใหญ่", mobileNumber = "097-890-1234", lineId = "nattapon")
        )

        // Each entry: owner index (into SAMPLE_OWNERS) -> pet template (ownerId filled in at insert time).
        private val SAMPLE_PETS = listOf(
            0 to PetEntity(
                ownerId = 0, name = "ข้าวปั้น", petType = PetType.DOG, breed = "พุดเดิ้ล",
                dateOfBirth = LocalDate.of(2021, 3, 15), gender = Gender.MALE, weight = 4.2f,
                color = "น้ำตาล", behaviorNotes = "ขี้อ้อน เป็นมิตร"
            ),
            1 to PetEntity(
                ownerId = 0, name = "น้ำตาล", petType = PetType.CAT, breed = "เปอร์เซีย",
                dateOfBirth = LocalDate.of(2020, 7, 1), gender = Gender.FEMALE, weight = 3.5f,
                color = "ครีม", allergies = "แพ้อาหารทะเล"
            ),
            1 to PetEntity(
                ownerId = 0, name = "ขนมปัง", petType = PetType.DOG, breed = "ชิวาวา",
                dateOfBirth = LocalDate.of(2022, 1, 20), gender = Gender.MALE, weight = 2.1f,
                color = "ขาว", behaviorNotes = "ตื่นกลัวคนแปลกหน้า"
            ),
            2 to PetEntity(
                ownerId = 0, name = "มะลิ", petType = PetType.DOG, breed = "ปอมเมอเรเนียน",
                dateOfBirth = LocalDate.of(2021, 11, 5), gender = Gender.FEMALE, weight = 3.0f,
                color = "ทอง"
            ),
            3 to PetEntity(
                ownerId = 0, name = "โกโก้", petType = PetType.DOG, breed = "โกลเด้น รีทรีฟเวอร์",
                dateOfBirth = LocalDate.of(2019, 5, 12), gender = Gender.MALE, weight = 28.0f,
                color = "ทอง", medications = "ยาบำรุงข้อต่อ"
            ),
            4 to PetEntity(
                ownerId = 0, name = "ลูกแก้ว", petType = PetType.CAT, breed = "สก็อตติช โฟลด์",
                dateOfBirth = LocalDate.of(2022, 4, 18), gender = Gender.FEMALE, weight = 3.2f,
                color = "เทา"
            ),
            4 to PetEntity(
                ownerId = 0, name = "ส้มโอ", petType = PetType.CAT, breed = "อเมริกัน ช็อตแฮร์",
                dateOfBirth = LocalDate.of(2021, 9, 9), gender = Gender.MALE, weight = 4.0f,
                color = "ส้ม", behaviorNotes = "ชอบเล่นน้ำ"
            ),
            5 to PetEntity(
                ownerId = 0, name = "มันนี่", petType = PetType.DOG, breed = "ชิห์สุ",
                dateOfBirth = LocalDate.of(2020, 12, 25), gender = Gender.FEMALE, weight = 5.5f,
                color = "น้ำตาล", allergies = "แพ้เกสรดอกไม้"
            ),
            6 to PetEntity(
                ownerId = 0, name = "เจ้าด่าง", petType = PetType.DOG, breed = "บางแก้ว",
                dateOfBirth = LocalDate.of(2018, 8, 30), gender = Gender.MALE, weight = 18.5f,
                color = "ดำขาว", behaviorNotes = "เฝ้าบ้านเก่ง ดุกับคนแปลกหน้า"
            ),
            7 to PetEntity(
                ownerId = 0, name = "ทองดี", petType = PetType.CAT, breed = "วิเชียรมาศ",
                dateOfBirth = LocalDate.of(2021, 6, 14), gender = Gender.MALE, weight = 4.3f,
                color = "ครีม"
            ),
            8 to PetEntity(
                ownerId = 0, name = "ขาวมณี", petType = PetType.CAT, breed = "พันธุ์ไทย",
                dateOfBirth = LocalDate.of(2020, 2, 2), gender = Gender.FEMALE, weight = 3.8f,
                color = "ขาว", notes = "ตาสองสี"
            ),
            9 to PetEntity(
                ownerId = 0, name = "ดำเนิน", petType = PetType.DOG, breed = "ลาบราดอร์",
                dateOfBirth = LocalDate.of(2019, 10, 10), gender = Gender.MALE, weight = 30.0f,
                color = "ดำ"
            ),
            9 to PetEntity(
                ownerId = 0, name = "ไข่มุก", petType = PetType.DOG, breed = "พุดเดิ้ล",
                dateOfBirth = LocalDate.of(2022, 3, 3), gender = Gender.FEMALE, weight = 4.8f,
                color = "ขาว", behaviorNotes = "ฉลาด เรียนรู้เร็ว"
            ),
            10 to PetEntity(
                ownerId = 0, name = "ปุยฝ้าย", petType = PetType.CAT, breed = "เปอร์เซีย",
                dateOfBirth = LocalDate.of(2021, 1, 28), gender = Gender.FEMALE, weight = 3.6f,
                color = "ขาว", medications = "หยอดตาประจำวัน"
            ),
            11 to PetEntity(
                ownerId = 0, name = "ช็อคโก้", petType = PetType.DOG, breed = "บีเกิ้ล",
                dateOfBirth = LocalDate.of(2020, 4, 22), gender = Gender.MALE, weight = 12.0f,
                color = "สามสี", behaviorNotes = "พลังเยอะ ชอบวิ่ง"
            ),
            12 to PetEntity(
                ownerId = 0, name = "ลัคกี้", petType = PetType.DOG, breed = "พันธุ์ผสม",
                dateOfBirth = LocalDate.of(2019, 7, 7), gender = Gender.MALE, weight = 15.0f,
                color = "น้ำตาล"
            ),
            13 to PetEntity(
                ownerId = 0, name = "มอคค่า", petType = PetType.CAT, breed = "เมนคูน",
                dateOfBirth = LocalDate.of(2020, 11, 11), gender = Gender.FEMALE, weight = 5.0f,
                color = "น้ำตาล", behaviorNotes = "ตัวใหญ่ ใจดี"
            ),
            14 to PetEntity(
                ownerId = 0, name = "แพนเค้ก", petType = PetType.DOG, breed = "ปอมเมอเรเนียน",
                dateOfBirth = LocalDate.of(2022, 5, 19), gender = Gender.FEMALE, weight = 2.8f,
                color = "ทอง"
            ),
            15 to PetEntity(
                ownerId = 0, name = "คุกกี้", petType = PetType.DOG, breed = "ไซบีเรียน ฮัสกี้",
                dateOfBirth = LocalDate.of(2019, 12, 1), gender = Gender.MALE, weight = 22.0f,
                color = "ดำขาว", allergies = "แพ้ไก่"
            ),
            16 to PetEntity(
                ownerId = 0, name = "บราวนี่", petType = PetType.CAT, breed = "พันธุ์ไทย",
                dateOfBirth = LocalDate.of(2021, 8, 8), gender = Gender.MALE, weight = 4.1f,
                color = "ดำ", notes = "ชอบนอนตัก"
            )
        )
    }
}
