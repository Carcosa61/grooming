package com.petgrooming.manager.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.ServiceType
import com.petgrooming.manager.domain.repository.ServicePriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServicePriceItem(
    val serviceType: ServiceType,
    val priceInput: String
)

data class ServicePriceUiState(
    val items: List<ServicePriceItem> = ServiceType.entries.map { ServicePriceItem(it, "") },
    val isLoading: Boolean = true,
    val saved: Boolean = false
)

@HiltViewModel
class ServicePriceViewModel @Inject constructor(
    private val servicePriceRepository: ServicePriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicePriceUiState())
    val uiState: StateFlow<ServicePriceUiState> = _uiState.asStateFlow()

    private var loaded = false

    init {
        loadPrices()
    }

    private fun loadPrices() {
        viewModelScope.launch {
            servicePriceRepository.getAllPrices().collect { prices ->
                // Only seed the editable inputs once so user edits aren't overwritten.
                if (!loaded) {
                    val priceMap = prices.associate { it.serviceType to it.price }
                    _uiState.value = _uiState.value.copy(
                        items = ServiceType.entries.map { type ->
                            ServicePriceItem(type, formatPrice(priceMap[type]))
                        },
                        isLoading = false
                    )
                    loaded = true
                }
            }
        }
    }

    fun updatePrice(serviceType: ServiceType, text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.serviceType == serviceType) it.copy(priceInput = filtered) else it
            },
            saved = false
        )
    }

    fun savePrices() {
        viewModelScope.launch {
            _uiState.value.items.forEach { item ->
                val price = item.priceInput.trim().toDoubleOrNull() ?: 0.0
                servicePriceRepository.setPrice(item.serviceType, price)
            }
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    fun consumeSaved() {
        _uiState.value = _uiState.value.copy(saved = false)
    }

    private fun formatPrice(price: Double?): String = when {
        price == null -> ""
        price == price.toLong().toDouble() -> price.toLong().toString()
        else -> price.toString()
    }
}
