import kotlinx.serialization.Serializable
import java.security.MessageDigest

interface MenuInteraction {
    fun addMenuItem(name: String, price: Int, preparationTime: Long, menu: Menu)
    fun removeMenuItem(name: String, menu: Menu)
    fun updateMenuItem(name: String, price: Int, preparationTime: Long, menu: Menu)
    fun updateDishQuantity(dish: Dish, newQuantity: Int, menu: Menu)
}

interface OrderInteraction {
    fun createOrder(visitor: Visitor)
    fun addItemToOrder(visitor: Visitor, itemName: String, menu: Menu)
    fun cancelOrder(visitor: Visitor)
    fun payOrder(visitor: Visitor, paymentMethod: PaymentMethod)
    fun submitOrderForProcessing(visitor: Visitor)
    fun writeReview(visitor: Visitor)
}

interface AdminActions {
    fun getMostPopularDish()
    fun getAverageRatingForDish()
    fun getOrdersCount()
}

@Serializable
sealed class User {
    abstract val id: Int
    abstract val username: String
    abstract val encryptedPassword: String
}

interface UserManager {
    fun viewMenu()
}


class VisitorManager(
    private val orderProcessor: OrderProcessor,
    private val payment: Payment,
    private val database: Database,
) : UserManager, OrderInteraction {
    fun registerUser(username: String, password: String) {
        val existingUser = database.users.find { it.username == username }
        if (existingUser == null) {
            val id = database.userIdCounter++
            database.users.add(Visitor(id, username, hashPassword(password)))
            println("Пользователь успешно зарегистрирован!")
        } else {
            println("Пользователь с таким именем уже существует!")
        }
    }

    fun loginUser(username: String, password: String): Boolean {
        val user = database.users.find { it.username == username }
        if (user != null) {
            return checkPassword(password, user.encryptedPassword)
        }
        return false;
    }

    private fun hashPassword(password: String): String {
        val digestBytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return digestBytes.joinToString("") { "%02x".format(it) }
    }

    private fun checkPassword(inputPassword: String, encryptedPassword: String): Boolean {
        return hashPassword(inputPassword) == encryptedPassword
    }

    override fun viewMenu() {
        println("Меню:")
        val dishesWithQuantity = database.menu.getDishes()
        dishesWithQuantity.forEach { dish ->
            println(dish)
        }
    }

    override fun createOrder(visitor: Visitor) {
        visitor.order = Order(1, mutableListOf(), OrderStatus.ACCEPTED, orderProcessor)
        database.orders.add(visitor.order!!)
    }

    override fun addItemToOrder(visitor: Visitor, itemName: String, menu: Menu) {
        val dishToAdd = menu.getDishes().find { it.name == itemName }

        if (dishToAdd != null) {

            if (dishToAdd.quantity > 0) {
                menu.updateDishQuantity(dishToAdd, dishToAdd.quantity - 1)
                visitor.order?.addItem(dishToAdd)
                println("Блюдо $itemName добавлено в заказ.")
            } else {
                println("Извините, но $itemName закончилось.")
            }
        } else {
            println("Блюдо $itemName не найдено в меню.")
        }
    }


    override fun cancelOrder(visitor: Visitor) {
        visitor.order?.cancelOrder()
    }

    override fun payOrder(visitor: Visitor, paymentMethod: PaymentMethod) {
        visitor.order?.payOrder(payment)
        database.totalRevenue += visitor.order?.calculateTotalPrice() ?: 0
    }

    override fun submitOrderForProcessing(visitor: Visitor) {
        visitor.order?.submitOrderForProcessing()
    }

    override fun writeReview(visitor: Visitor) {
        val order = visitor.order
        if (order != null) {
            order.items.forEach { dish ->
                order.leaveReview(dish)
            }
            order.updateStatus(OrderStatus.REVIEWED)
            println("Отзывы оставлены!")
        } else {
            println("Не удалось оставить отзывы. Заказ не найден.")
        }
    }

}


class AdminManager(private val database: Database) : UserManager, MenuInteraction, AdminActions {
    fun registerUser(username: String, password: String) {
        val existingUser = database.users.find { it.username == username }
        if (existingUser == null) {
            val id = database.userIdCounter++
            database.users.add(Administrator(id, username, hashPassword(password)))
            println("Пользователь успешно зарегистрирован!")
        } else {
            println("Пользователь с таким именем уже существует!")
        }
    }

    fun loginUser(username: String, password: String): Boolean {
        val user = database.users.find { it.username == username }
        if (user != null) {
            return checkPassword(password, user.encryptedPassword)
        }
        return false;
    }

    private fun hashPassword(password: String): String {
        val digestBytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return digestBytes.joinToString("") { "%02x".format(it) }
    }

    private fun checkPassword(inputPassword: String, encryptedPassword: String): Boolean {
        return hashPassword(inputPassword) == encryptedPassword
    }

    override fun viewMenu() {
        println("Меню:")
        val dishes = database.menu.getDishes()
        dishes.forEach { dish ->
            println("Блюдо: ${dish.name}, Цена: ${dish.price}, Время приготовления: ${dish.cookingTime}, Количество: ${dish.quantity}")
        }
    }

    override fun addMenuItem(name: String, price: Int, preparationTime: Long, menu: Menu) {
        val newDish = Dish(name, price, preparationTime, 1)
        menu.addDish(newDish)
    }

    override fun removeMenuItem(name: String, menu: Menu) {
        val dishToRemove = menu.getDishes().find { it.name == name }
        if (dishToRemove != null) {
            menu.removeDish(dishToRemove)
            println("Блюдо $name удалено из меню!")
        } else {
            println("Блюдо $name не найдено в меню!.")
        }
    }

    override fun updateMenuItem(name: String, price: Int, preparationTime: Long, menu: Menu) {
        val dishToUpdate = menu.getDishes().find { it.name == name }
        if (dishToUpdate != null) {
            dishToUpdate.cookingTime = preparationTime
            dishToUpdate.price = price
            println("Updated $name in the menu")
        } else {
            println("Блюдо $name не найдено в меню!.")
        }
    }

    override fun updateDishQuantity(dish: Dish, newQuantity: Int, menu: Menu) {
        menu.updateDishQuantity(dish, newQuantity)
    }

    override fun getMostPopularDish() {
        val popularDishes = database.menu.getDishes()
            .filter { it.reviews.isNotEmpty() }
            .maxByOrNull { it.reviews.size }

        popularDishes?.let {
            val averageRating = it.reviews.map { review -> review.rating }
                .average()
            println("Самое популярное блюдо: ${it.name}")
            println("Число заказов блюда: ${it.reviews.size}")
            println("Средний рейтинг: ${"%.2f".format(averageRating)}")
        } ?: println("Нет данных о популярных блюдах.")
    }

    override fun getAverageRatingForDish() {
        for (dish in database.menu.getDishes()) {
            val reviews = dish.reviews.map { it.rating }

            if (reviews.isNotEmpty()) {
                val averageRating = reviews.average()
                println("Средний рейтинг для блюда ${dish.name}: ${"%.2f".format(averageRating)}")
            } else {
                println("Нет данных о рейтинге для блюда ${dish.name}.")
            }
        }
    }

    override fun getOrdersCount() {
        val ordersCount = database.orders.size
        val totalRevenue = database.totalRevenue

        println("Общее число заказов: $ordersCount")
        println("Суммарный доход: $totalRevenue")
    }

}

@Serializable
class Administrator(override val id: Int, override val username: String, override var encryptedPassword: String) :
    User() {
}


@Serializable
class Visitor(override val id: Int, override val username: String, override var encryptedPassword: String) : User() {
    var order: Order? = null
}
