package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class BusinessRepository(
    private val userDao: UserDao,
    private val productDao: ProductDao,
    private val checklistDao: ChecklistDao
) {
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun registerUser(user: User) {
        userDao.insertUser(user)
    }

    fun getProducts(userEmail: String): Flow<List<Product>> {
        return productDao.getProductsByUser(userEmail)
    }

    suspend fun saveProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProductById(id: Long) {
        productDao.deleteProductById(id)
    }

    suspend fun getChecklist(userEmail: String, dateString: String): DailyChecklist? {
        val key = "${dateString}_$userEmail"
        return checklistDao.getChecklist(key)
    }

    suspend fun saveChecklist(checklist: DailyChecklist) {
        checklistDao.saveChecklist(checklist)
    }

    suspend fun preseedDemoProducts(email: String) {
        // If there are already products, do not pre-seed
        val productsFlow = productDao.getProductsByUser(email)
        val existing = productsFlow.firstOrNull() ?: emptyList()
        if (existing.isEmpty()) {
            val demoList = listOf(
                Product(
                    userEmail = email,
                    name = "Kopi Arabika Premium",
                    category = "Minuman",
                    costPrice = 5000.0,
                    sellingPrice = 15000.0,
                    stock = 100,
                    description = "Kopi arabika pilihan dengan aroma kuat"
                ),
                Product(
                    userEmail = email,
                    name = "Teh Hijau Matcha",
                    category = "Minuman",
                    costPrice = 3000.0,
                    sellingPrice = 12000.0,
                    stock = 80,
                    description = "Teh hijau matcha jepang asli"
                ),
                Product(
                    userEmail = email,
                    name = "Croissant Butter",
                    category = "Makanan",
                    costPrice = 8000.0,
                    sellingPrice = 18000.0,
                    stock = 30,
                    description = "Lapis renyah mentega prancis"
                ),
                Product(
                    userEmail = email,
                    name = "Es Teh Manis",
                    category = "Minuman",
                    costPrice = 1500.0,
                    sellingPrice = 5000.0,
                    stock = 5,
                    description = "Es teh manis menyegarkan"
                ),
                Product(
                    userEmail = email,
                    name = "Roti Bakar Coklat",
                    category = "Makanan",
                    costPrice = 6000.0,
                    sellingPrice = 14000.0,
                    stock = 25,
                    description = "Roti panggang dengan meses coklat premium"
                )
            )
            productDao.insertProducts(demoList)
        }
    }
}
