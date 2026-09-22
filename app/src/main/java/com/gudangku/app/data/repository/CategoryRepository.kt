package com.gudangku.app.data.repository

import com.gudangku.app.data.dao.CategoryDao
import com.gudangku.app.data.entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insert(category: Category): Long = categoryDao.insert(category)

    suspend fun deleteById(categoryId: Long) = categoryDao.deleteById(categoryId)
}
