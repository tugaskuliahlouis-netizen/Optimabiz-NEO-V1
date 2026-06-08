package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ============================================================
// ENTITIES
// ============================================================

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val bizName: String,
    val category: String,
    val plan: String = "free",
    val profileImageUri: String? = null
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val name: String,
    val category: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val stock: Int,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_checklist")
data class DailyChecklist(
    @PrimaryKey val dateKey: String, // format: "YYYY-MM-DD_email"
    val userEmail: String,
    val dateString: String,
    val checkedIds: String // comma-separated check IDs
)

// ============================================================
// DAOS
// ============================================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE userEmail = :email ORDER BY id DESC")
    fun getProductsByUser(email: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM daily_checklist WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getChecklist(dateKey: String): DailyChecklist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChecklist(checklist: DailyChecklist)
}

// ============================================================
// DATABASE
// ============================================================

@Database(entities = [User::class, Product::class, DailyChecklist::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun checklistDao(): ChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "optimabiz_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
