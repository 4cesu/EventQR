package com.thedavelopers.eventqr.core.api.dto

/**
 * Mirrors Spring Boot's `Page<T>` serialization.
 * Unknown fields (pageable, sort) are silently ignored by Gson.
 */
data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val empty: Boolean = true,
)
