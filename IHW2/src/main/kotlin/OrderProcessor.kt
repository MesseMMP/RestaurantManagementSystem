import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue


class OrderProcessor private constructor() {
    private val executor = Executors.newFixedThreadPool(2) // Пул из 2 потоков (кухня готовит одновременно 2 заказа)
    private val orderQueue: BlockingQueue<Order> =
        PriorityBlockingQueue(10, compareByDescending<Order> { it.calculateTotalPrice() })

    fun processOrder(order: Order) {
        orderQueue.put(order)
    }

    init {
        // Запускаем обработку заказов в отдельном потоке
        Thread {
            while (true) {
                val order = orderQueue.take() // Блокирующая операция, ждем, пока не появится заказ
                executor.submit {
                    order.updateStatus(OrderStatus.IN_PROGRESS)
                    // Обработка заказа
                    val executionTime: Long = order.items.sumOf { d -> d.cookingTime }
                    for (i in 0 until executionTime) {
                        if (order.isCanceled()) {
                            return@submit
                        }
                        Thread.sleep(1000)
                    }

                    if (!order.isCanceled()) {
                        order.updateStatus(OrderStatus.READY)
                    }
                }
            }
        }.start()
    }

    companion object {
        private val INSTANCE = OrderProcessor()

        fun getInstance(): OrderProcessor {
            return INSTANCE
        }
    }
}

