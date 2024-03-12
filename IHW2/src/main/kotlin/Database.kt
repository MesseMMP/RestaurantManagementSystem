import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class Database private constructor() {
    var userIdCounter: Int = 0
    var users: MutableList<User> = mutableListOf()
    var orders: MutableList<Order> = mutableListOf()
    var menu: Menu = Menu()
    var totalRevenue: Int = 0

    companion object {
        private val INSTANCE = Database()

        fun getInstance(): Database {
            return INSTANCE
        }
    }
}

class DataManager(private var database: Database) {
    private val dataFolderPath = "data"

    init {
        // Создаем папку "data", если её нет
        File(dataFolderPath).mkdirs()
    }

    fun saveData() {
        try {
            val json = Json.encodeToString(database)
            val filePath = "$dataFolderPath/restaurant.json"
            File(filePath).writeText(json)
            println("Данные о системе успешно сохранены!")
        } catch (e: Exception) {
            println("Ошибка при сохранении данных системе: ${e.message}")
        }
    }

    fun loadData() {
        try {
            val filePath = "$dataFolderPath/restaurant.json"
            val json = File(filePath).readText()
            val newDatabase: Database = Json.decodeFromString(json)
            database.userIdCounter = newDatabase.userIdCounter
            database.users = newDatabase.users
            database.orders = newDatabase.orders
            database.menu = newDatabase.menu
            database.totalRevenue = newDatabase.totalRevenue
            println("Данные о системе успешно закгружены!")
        } catch (e: Exception) {
            println("Ошибка при загрузке данных о системе: ${e.message}")
        }
    }
}
