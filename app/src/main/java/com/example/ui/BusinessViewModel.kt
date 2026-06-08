package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    LANDING,
    LOGIN,
    SIGNUP,
    MAIN_APP
}

enum class AppTab {
    DASHBOARD,
    PRODUCTS,
    ANALYSIS,
    STRATEGY,
    PROFILE
}

class BusinessViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BusinessRepository(db.userDao(), db.productDao(), db.checklistDao())

    // ============================================================
    // NAVIGATION STATE
    // ============================================================
    private val _currentScreen = MutableStateFlow(AppScreen.LANDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // ============================================================
    // AUTH STATE
    // ============================================================
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // ============================================================
    // PRODUCTS STATE
    // ============================================================
    val productsList: StateFlow<List<Product>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user ->
            repository.getProducts(user.email)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow("all")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    // ============================================================
    // DIALOG & FORM STATES
    // ============================================================
    private val _showProductDialog = MutableStateFlow(false)
    val showProductDialog: StateFlow<Boolean> = _showProductDialog.asStateFlow()

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    private val _pendingDeleteId = MutableStateFlow<Long?>(null)
    val pendingDeleteId: StateFlow<Long?> = _pendingDeleteId.asStateFlow()

    // ============================================================
    // TOAST NOTIFICATIONS
    // ============================================================
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _toastType = MutableStateFlow("success") // "success" or "error"
    val toastType: StateFlow<String> = _toastType.asStateFlow()

    // ============================================================
    // STRATEGY TAB & WORK STATES
    // ============================================================
    private val _activeStrategyTab = MutableStateFlow("tips") // "tips", "whatif", "checklist", "recs"
    val activeStrategyTab: StateFlow<String> = _activeStrategyTab.asStateFlow()

    // What-If Calculator State
    private val _wifiSelectedProductId = MutableStateFlow<Long?>(null)
    val wifiSelectedProductId: StateFlow<Long?> = _wifiSelectedProductId.asStateFlow()

    private val _wifiNewSellPrice = MutableStateFlow("")
    val wifiNewSellPrice: StateFlow<String> = _wifiNewSellPrice.asStateFlow()

    private val _wifiNewCostPrice = MutableStateFlow("")
    val wifiNewCostPrice: StateFlow<String> = _wifiNewCostPrice.asStateFlow()

    // Checklist State
    private val _checkedItemIds = MutableStateFlow<List<String>>(emptyList())
    val checkedItemIds: StateFlow<List<String>> = _checkedItemIds.asStateFlow()

    // recommendations completed ids / dismissed ids
    private val _completedRecIds = MutableStateFlow<Set<String>>(emptySet())
    val completedRecIds: StateFlow<Set<String>> = _completedRecIds.asStateFlow()

    private val _dismissedRecIds = MutableStateFlow<Set<String>>(emptySet())
    val dismissedRecIds: StateFlow<Set<String>> = _dismissedRecIds.asStateFlow()

    // ============================================================
    // PROFILE STATE FOR EDITING
    // ============================================================
    private val _editBizName = MutableStateFlow("")
    val editBizName: StateFlow<String> = _editBizName.asStateFlow()

    private val _editCategory = MutableStateFlow("F&B")
    val editCategory: StateFlow<String> = _editCategory.asStateFlow()

    init {
        // Try auto-loading saved session if possible (Room or simple state)
        // Since we are inside AI Studio, we keep session in memory, but could save local cache.
    }

    // ============================================================
    // AUTH LOGIC
    // ============================================================
    fun handleSignup(bizName: String, category: String, email: String, password: String) {
        if (bizName.isBlank() || category.isBlank() || email.isBlank() || password.isBlank()) {
            showToast("Semua field wajib diisi!", "error")
            return
        }
        if (password.length < 6) {
            showToast("Password minimal 6 karakter!", "error")
            return
        }
        if (!email.contains("@")) {
            showToast("Format email tidak valid!", "error")
            return
        }

        viewModelScope.launch {
            val existing = repository.getUserByEmail(email.lowercase().trim())
            if (existing != null) {
                showToast("Email sudah terdaftar!", "error")
                return@launch
            }

            val newUser = User(
                email = email.lowercase().trim(),
                passwordHash = password, // Simplified
                bizName = bizName.trim(),
                category = category
            )
            repository.registerUser(newUser)
            _currentUser.value = newUser
            _editBizName.value = newUser.bizName
            _editCategory.value = newUser.category
            _currentScreen.value = AppScreen.MAIN_APP
            _currentTab.value = AppTab.DASHBOARD
            loadChecklistForCurrentUser()
            showToast("Akun berhasil dibuat! 🎉", "success")
        }
    }

    fun handleLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            showToast("Email dan password wajib diisi!", "error")
            return
        }

        val cleanEmail = email.lowercase().trim()

        viewModelScope.launch {
            // Demo account shortcut
            if (cleanEmail == "demo@optimabiz.com" && password == "demo123") {
                val demoUser = User(
                    email = "demo@optimabiz.com",
                    passwordHash = "demo123",
                    bizName = "Toko Kopi Demo",
                    category = "F&B"
                )
                repository.registerUser(demoUser) // Ensure exists
                repository.preseedDemoProducts("demo@optimabiz.com")

                _currentUser.value = demoUser
                _editBizName.value = demoUser.bizName
                _editCategory.value = demoUser.category
                _currentScreen.value = AppScreen.MAIN_APP
                _currentTab.value = AppTab.DASHBOARD
                loadChecklistForCurrentUser()
                showToast("Selamat datang di Demo! 🚀", "success")
                return@launch
            }

            val user = repository.getUserByEmail(cleanEmail)
            if (user == null || user.passwordHash != password) {
                showToast("Email atau password salah!", "error")
                return@launch
            }

            _currentUser.value = user
            _editBizName.value = user.bizName
            _editCategory.value = user.category
            _currentScreen.value = AppScreen.MAIN_APP
            _currentTab.value = AppTab.DASHBOARD
            loadChecklistForCurrentUser()
            showToast("Selamat datang kembali! 👋", "success")
        }
    }

    fun handleLogout() {
        _currentUser.value = null
        _checkedItemIds.value = emptyList()
        _completedRecIds.value = emptySet()
        _dismissedRecIds.value = emptySet()
        _currentScreen.value = AppScreen.LANDING
        _currentTab.value = AppTab.DASHBOARD
        showToast("Berhasil keluar", "success")
    }

    fun updateProfile(newName: String, newCategory: String) {
        val user = _currentUser.value ?: return
        if (newName.isBlank()) {
            showToast("Nama bisnis tidak boleh kosong!", "error")
            return
        }

        viewModelScope.launch {
            val updated = user.copy(bizName = newName.trim(), category = newCategory)
            repository.registerUser(updated)
            _currentUser.value = updated
            showToast("Profil berhasil diperbarui ✅", "success")
        }
    }

    fun updateProfilePhoto(uriString: String?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(profileImageUri = uriString)
            repository.registerUser(updated)
            _currentUser.value = updated
            showToast("Foto profil diperbarui! 📸", "success")
        }
    }

    // ============================================================
    // NAVIGATION METHODS
    // ============================================================
    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setStrategyTab(subTab: String) {
        _activeStrategyTab.value = subTab
    }

    // ============================================================
    // PRODUCTS MANAGEMENT
    // ============================================================
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun openAddProduct() {
        _editingProduct.value = null
        _showProductDialog.value = true
    }

    fun openEditProduct(product: Product) {
        _editingProduct.value = product
        _showProductDialog.value = true
    }

    fun closeProductDialog() {
        _showProductDialog.value = false
        _editingProduct.value = null
    }

    fun requestDeleteProduct(productId: Long) {
        _pendingDeleteId.value = productId
        _showDeleteConfirm.value = true
    }

    fun cancelDeleteProduct() {
        _pendingDeleteId.value = null
        _showDeleteConfirm.value = false
    }

    fun confirmDeleteProduct() {
        val id = _pendingDeleteId.value ?: return
        viewModelScope.launch {
            repository.deleteProductById(id)
            showToast("Produk berhasil dihapus 🗑️", "success")
            _pendingDeleteId.value = null
            _showDeleteConfirm.value = false
            // Reset what-if if deleted
            if (_wifiSelectedProductId.value == id) {
                _wifiSelectedProductId.value = null
            }
        }
    }

    fun saveProduct(
        name: String,
        category: String,
        costPrice: Double,
        sellingPrice: Double,
        stock: Int,
        description: String
    ) {
        val user = _currentUser.value ?: return
        if (name.isBlank()) {
            showToast("Nama produk wajib diisi!", "error")
            return
        }
        if (costPrice <= 0) {
            showToast("Harga modal harus lebih dari 0!", "error")
            return
        }
        if (sellingPrice <= 0) {
            showToast("Harga jual harus lebih dari 0!", "error")
            return
        }
        if (sellingPrice < costPrice) {
            showToast("Harga jual tidak boleh kurang dari harga modal!", "error")
            return
        }

        viewModelScope.launch {
            val productToSave = _editingProduct.value?.copy(
                name = name.trim(),
                category = category,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                stock = stock,
                description = description.trim()
            ) ?: Product(
                userEmail = user.email,
                name = name.trim(),
                category = category,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                stock = stock,
                description = description.trim()
            )

            repository.saveProduct(productToSave)
            showToast("Produk berhasil disimpan ✅", "success")
            closeProductDialog()
        }
    }

    // ============================================================
    // TOAST HELPERS
    // ============================================================
    fun showToast(message: String, type: String = "success") {
        _toastMessage.value = message
        _toastType.value = type
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // ============================================================
    // WHAT-IF CALCULATOR LOGIC
    // ============================================================
    fun setWifiProduct(productId: Long?) {
        _wifiSelectedProductId.value = productId
        val p = productsList.value.find { it.id == productId }
        if (p != null) {
            _wifiNewSellPrice.value = p.sellingPrice.toInt().toString()
            _wifiNewCostPrice.value = p.costPrice.toInt().toString()
        } else {
            _wifiNewSellPrice.value = ""
            _wifiNewCostPrice.value = ""
        }
    }

    fun updateWifiSellPrice(price: String) {
        _wifiNewSellPrice.value = price
    }

    fun updateWifiCostPrice(price: String) {
        _wifiNewCostPrice.value = price
    }

    // ============================================================
    // CHECKLIST LOGIC
    // ============================================================
    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun loadChecklistForCurrentUser() {
        val user = _currentUser.value ?: return
        val dateStr = getTodayDateString()
        viewModelScope.launch {
            val checklist = repository.getChecklist(user.email, dateStr)
            if (checklist != null) {
                val ids = if (checklist.checkedIds.isBlank()) emptyList() else checklist.checkedIds.split(",")
                _checkedItemIds.value = ids
            } else {
                _checkedItemIds.value = emptyList()
            }
        }
    }

    fun toggleChecklistItem(itemId: String) {
        val user = _currentUser.value ?: return
        val dateStr = getTodayDateString()
        val currentList = _checkedItemIds.value.toMutableList()

        if (currentList.contains(itemId)) {
            currentList.remove(itemId)
        } else {
            currentList.add(itemId)
        }
        _checkedItemIds.value = currentList

        viewModelScope.launch {
            val csv = currentList.joinToString(",")
            val entry = DailyChecklist(
                dateKey = "${dateStr}_${user.email}",
                userEmail = user.email,
                dateString = dateStr,
                checkedIds = csv
            )
            repository.saveChecklist(entry)
        }
    }

    fun resetChecklist() {
        val user = _currentUser.value ?: return
        val dateStr = getTodayDateString()
        _checkedItemIds.value = emptyList()
        viewModelScope.launch {
            val entry = DailyChecklist(
                dateKey = "${dateStr}_${user.email}",
                userEmail = user.email,
                dateString = dateStr,
                checkedIds = ""
            )
            repository.saveChecklist(entry)
            showToast("Checklist berhasil direset ✅", "success")
        }
    }

    // ============================================================
    // AI RECOMMENDATIONS & ACTIONS
    // ============================================================
    fun markRecComplete(recId: String) {
        _completedRecIds.value = _completedRecIds.value + recId
        showToast("Aksi berhasil dilakukan! 🎉", "success")
    }

    fun dismissRec(recId: String) {
        _dismissedRecIds.value = _dismissedRecIds.value + recId
    }

    fun resetRecommendations() {
        _completedRecIds.value = emptySet()
        _dismissedRecIds.value = emptySet()
        showToast("Rekomendasi di-refresh", "success")
    }

    // ============================================================
    // INTERACTIVE MENTOR AI STATE & LOGIC
    // ============================================================
    private val _mentorMessages = MutableStateFlow<List<MentorMessage>>(listOf(
        MentorMessage(
            isUser = false,
            message = "Halo! Saya adalah Mentor Bisnis AI Anda. Saya siap membantu menganalisis produk, margin profit, dan strategi pertumbuhan untuk usaha Anda. Apa yang ingin Anda tanyakan hari ini?"
        )
    ))
    val mentorMessages: StateFlow<List<MentorMessage>> = _mentorMessages.asStateFlow()

    private val _isMentorLoading = MutableStateFlow(false)
    val isMentorLoading: StateFlow<Boolean> = _isMentorLoading.asStateFlow()

    private fun localFormatRupiah(value: Double): String {
        return "Rp " + String.format(Locale("id", "ID"), "%,.0f", value)
    }

    fun sendQuestionToMentor(question: String) {
        if (question.isBlank()) return

        val userMsg = MentorMessage(isUser = true, message = question)
        _mentorMessages.value = _mentorMessages.value + userMsg
        _isMentorLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Collect contextual metadata from current products!
                val user = _currentUser.value
                val products = productsList.value
                val bizCategory = user?.category ?: "F&B"
                val bizName = user?.bizName ?: "Maju Usaha"

                val productDetails = products.joinToString("\n") { p ->
                    "- ${p.name}: Kategori: ${p.category}, Harga Beli: ${localFormatRupiah(p.costPrice)}, Harga Jual: ${localFormatRupiah(p.sellingPrice)}, Stok: ${p.stock}"
                }

                val totalInvValue = products.sumOf { p -> p.costPrice * p.stock }
                val totalStk = products.sumOf { p -> p.stock }

                val systemContext = """
                    Anda adalah Mentor Bisnis AI profesional untuk UMKM Indonesia.
                    Nama Bisnis Pengguna: ${bizName}
                    Kategori Bisnis: ${bizCategory}
                    Total Produk Terdaftar: ${products.size}
                    Total Stok: ${totalStk} unit
                    Nilai Total Inventaris Usaha: ${localFormatRupiah(totalInvValue.toDouble())}

                    Data Produk Detail:
                    ${productDetails}

                    Panduan untuk memberikan respons:
                    1. Berikan solusi mentor bisnis yang sangat praktis, bimbingan taktis, dan mudah dipahami UMKM Indonesia.
                    2. Gunakan bahasa Indonesia yang ramah, profesional, menyemangati, dan edukatif (Gunakan panggilan 'Kakak' atau 'Kak').
                    3. Berikan analisis berbasis data berdasarkan data produk di atas apabila relevan dengan pertanyaan user.
                    4. Berikan saran taktis, strategi peningkatan profit margin, promosi produk unggulan, atau pengelolaan stok. Jangan memberikan saran yang tidak realistis untuk UMKM kecil.
                    
                    Pertanyaan Pengguna:
                    ${question}
                """.trimIndent()

                val client = OkHttpClient.Builder()
                    .connectTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val requestJson = JSONObject()
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", systemContext)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                requestJson.put("contents", contentsArray)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                
                val apiKey = BuildConfig.GEMINI_API_KEY
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val root = JSONObject(responseStr)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .optString("text") ?: "Maaf, saya tidak dapat merumuskan saran untuk saat ini Kak."
                        
                        _mentorMessages.value = _mentorMessages.value + MentorMessage(isUser = false, message = text)
                    } else {
                        _mentorMessages.value = _mentorMessages.value + MentorMessage(isUser = false, message = "Maaf Kak, Mentor AI sedang tidak mendapat respons. Silakan klik kirim ulang.")
                    }
                } else {
                    _mentorMessages.value = _mentorMessages.value + MentorMessage(isUser = false, message = "Maaf Kak, gagal menghubungi Mentor AI (Error ${response.code}). Silakan coba sesaat lagi.")
                }
            } catch (e: Exception) {
                _mentorMessages.value = _mentorMessages.value + MentorMessage(isUser = false, message = "Koneksi terputus. Silakan pastikan perangkat terhubung ke internet dan API Key Anda valid.")
            } finally {
                _isMentorLoading.value = false
            }
        }
    }
}

data class MentorMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
