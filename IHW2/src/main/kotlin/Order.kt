import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class OrderStatus {
    ACCEPTED, IN_PROGRESS, READY, CANCELLED, PAID, REVIEWED
}

@Serializable
class Order(
    var orderId: Int,
    val items: MutableList<Dish>,
    var status: OrderStatus,
    @Transient private val orderProcessor: OrderProcessor = OrderProcessor.getInstance()
) {
    companion object {
        private var orderIdCounter = 1
        private val orderIdMutex = Mutex()
    }

    init {
        orderId = generateOrderId()
    }

    fun calculateTotalPrice(): Int {
        return items.sumBy { it.price }
    }

    private fun generateOrderId(): Int = runBlocking {
        orderIdMutex.withLock {
            val generatedId = orderIdCounter
            orderIdCounter++
            generatedId
        }
    }

    fun updateStatus(newStatus: OrderStatus) {
        status = newStatus
        println("Статус заказа $orderId изменен на $newStatus")
    }

    fun addItem(dish: Dish) {
        if (status == OrderStatus.ACCEPTED || status == OrderStatus.IN_PROGRESS) {
            items.add(dish)
            println("Добавлено блюдо ${dish.name} в заказ $orderId")
        } else {
            println("Нельзя добавить блюдо в заказ $orderId. Статус заказа: $status")
        }
    }

    fun cancelOrder() {
        if (status != OrderStatus.READY) {
            updateStatus(OrderStatus.CANCELLED)
        } else {
            println("Нельзя отменить заказ $orderId. Заказ уже готов!")
        }
    }

    fun payOrder(payment: Payment) {
        if (status == OrderStatus.READY) {
            payment.processPayment(this, PaymentMethod.CASH)
            updateStatus(OrderStatus.PAID)
        } else {
            println("Нельзя оплатить заказ $orderId. Заказ еще не готов!")
        }
    }

    fun submitOrderForProcessing() {
        orderProcessor.processOrder(this)
    }

    fun isCanceled(): Boolean {
        return status == OrderStatus.CANCELLED
    }


    fun leaveReview(dish: Dish) {
        println("Оцените блюдо ${dish.name} от 1 до 5:")
        val rating = readRatingInput()
        println("Введите текст отзыва для блюда ${dish.name}:")
        val comment = readln()
        if (status == OrderStatus.PAID) {
            if (rating in 1..5) {
                val dishToReview = items.find { it == dish }
                dishToReview?.reviews?.add(Review(rating, comment))
                println("Отзыв успешно добавлен к блюду ${dish.name} в заказе $orderId.")
            } else {
                println("Ошибка: Рейтинг должен быть в диапазоне от 1 до 5.")
            }
        } else {
            println("Нельзя оставить отзыв к блюду ${dish.name} в заказе $orderId. Заказ должен быть оплачен.")
        }
    }

    private fun readRatingInput(): Int {
        val input = readIntInput()

        if (input in 1..5) {
            return input
        } else {
            println("Ошибка ввода! Оценка должна быть в диапазоне от 1 до 5.")
            return readRatingInput()
        }
    }

    private fun readIntInput(): Int {
        return try {
            readlnOrNull()?.toInt() ?: 0
        } catch (e: NumberFormatException) {
            println("Ошибка ввода! Введите корректное число.")
            readIntInput()
        }
    }
}


enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    QR_CODE,
}

@Serializable
class Payment private constructor() {
    companion object {
        private val INSTANCE = Payment()

        fun getInstance(): Payment {
            return INSTANCE
        }
    }

    fun processPayment(order: Order, paymentMethod: PaymentMethod) {
        println("Заказ ${order.orderId} был оплачен ${paymentMethod.name}.")
    }
}

@Serializable
class Review(
    val rating: Int,
    val comment: String
)