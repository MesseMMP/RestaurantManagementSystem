class Restaurant(orderProcessor: OrderProcessor, payment: Payment, database: Database) : MenuInteraction,
    OrderInteraction {
    private val visitorManager = VisitorManager(orderProcessor, payment, database)
    private val adminManager = AdminManager(database)
    private val dataManager = DataManager(database)

    fun registerVisitor(username: String, password: String) {
        visitorManager.registerUser(username, password)
    }

    fun registerAdmin(username: String, password: String) {
        adminManager.registerUser(username, password)
    }

    fun loginVisitor(username: String, password: String): Boolean {
        return visitorManager.loginUser(username, password)
    }

    fun loginAdmin(username: String, password: String): Boolean {
        return adminManager.loginUser(username, password)
    }

    override fun addMenuItem(name: String, price: Int, preparationTime: Long, menu: Menu) {
        adminManager.addMenuItem(name, price, preparationTime, menu)
    }

    override fun removeMenuItem(name: String, menu: Menu) {
        adminManager.removeMenuItem(name, menu)
    }

    override fun updateMenuItem(name: String, price: Int, preparationTime: Long, menu: Menu) {
        adminManager.updateMenuItem(name, price, preparationTime, menu)
    }

    override fun updateDishQuantity(dish: Dish, newQuantity: Int, menu: Menu) {
        adminManager.updateDishQuantity(dish, newQuantity, menu)
    }

    override fun createOrder(visitor: Visitor) {
        visitorManager.createOrder(visitor)
    }

    override fun addItemToOrder(visitor: Visitor, itemName: String, menu: Menu) {
        visitorManager.addItemToOrder(visitor, itemName, menu)
    }

    override fun cancelOrder(visitor: Visitor) {
        visitorManager.cancelOrder(visitor)
    }

    override fun payOrder(visitor: Visitor, paymentMethod: PaymentMethod) {
        visitorManager.payOrder(visitor, paymentMethod)
    }

    override fun submitOrderForProcessing(visitor: Visitor) {
        visitorManager.submitOrderForProcessing(visitor)
    }

    override fun writeReview(visitor: Visitor) {
        visitorManager.writeReview(visitor)
    }

    fun viewVisitorMenu() {
        visitorManager.viewMenu()
    }

    fun viewAdminMenu() {
        adminManager.viewMenu()
    }

    fun loadData() {
        dataManager.loadData()
    }

    fun saveData() {
        dataManager.saveData()
    }

    fun printMostPopularDishes() {
        adminManager.getMostPopularDish()
    }

    fun printAverageRatingForDishes() {
        adminManager.getAverageRatingForDish()
    }

    fun printOrdersCountAndRevenue() {
        adminManager.getOrdersCount()
    }
}
