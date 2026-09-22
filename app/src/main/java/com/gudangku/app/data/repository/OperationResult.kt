package com.gudangku.app.data.repository

/**
 * Hasil sederhana untuk operasi repository yang bisa gagal karena validasi
 * (misal: SKU duplikat, stok tidak cukup) tanpa harus melempar exception.
 */
sealed class OperationResult {
    object Success : OperationResult()
    data class Error(val message: String) : OperationResult()
}
