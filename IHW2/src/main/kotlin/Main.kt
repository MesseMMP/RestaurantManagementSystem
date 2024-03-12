fun main() {
    val orderProcessor = OrderProcessor.getInstance()
    val payment = Payment.getInstance()
    val database = Database.getInstance()
    val restaurant = RestaurantApplication(orderProcessor, payment, database)
    restaurant.run()
}