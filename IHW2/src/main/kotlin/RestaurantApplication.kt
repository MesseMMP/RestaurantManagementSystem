import java.util.*
import kotlin.system.exitProcess

class RestaurantApplication(
    orderProcessor: OrderProcessor,
    payment: Payment,
    private val database: Database
) {
    private val restaurant: Restaurant = Restaurant(orderProcessor, payment, database)
    private val scanner = Scanner(System.`in`)
    private var currentUser: User? = null

    fun run() {
        try {
            loadData()
            while (true) {
                if (currentUser == null) {
                    startMenu()
                } else {
                    if (currentUser is Visitor) {
                        val visitor = currentUser as Visitor
                        if (visitor.order == null) {
                            handleCommandsNone()
                        } else {
                            when (visitor.order!!.status) {
                                OrderStatus.ACCEPTED -> {
                                    handleCommandsAccepted()
                                }

                                OrderStatus.IN_PROGRESS -> {
                                    handleCommandsPreparing()
                                }

                                OrderStatus.READY -> {
                                    handleCommandsReady()
                                }

                                OrderStatus.PAID -> {
                                    handleCommandsPaid()
                                }

                                else -> {
                                    handleCommandsOther()
                                }
                            }
                        }
                    } else {
                        handleAdminCommands()
                    }
                }
                // Небольшая пауза для обновления статуса заказа
                Thread.sleep(100)
            }
        } catch (e: Exception) {
            println("Произошла непредвиденная ошибка: ${e.message}")
        }
    }

    private fun startMenu() {
        println("=== Вы посетитель или админ? ===")
        println("1. Посетитель")
        println("2. Админ")
        println("0. Завершение работы программы")

        when (scanner.nextLine()) {
            "1" -> {
                handleVisitorAuthentication()
            }

            "2" -> {
                handleAdminAuthentication()
            }

            "0" -> {
                saveData()
                println("Программа завершена.")
                exitProcess(0)
            }

            else -> println("Некорректный ввод! Попробуйте еще раз!")
        }
    }

    private fun handleVisitorAuthentication() {
        println("=== Добро пожаловать в Ресторан ===")
        println("1. Регистрация")
        println("2. Вход")
        println("0. Завершение работы программы")

        when (scanner.nextLine()) {
            "1" -> {
                handleVisitorRegistration()
            }

            "2" -> {
                handleVisitorLogin()
            }

            "0" -> {
                saveData()
                println("Программа завершена.")
                exitProcess(0)
            }

            else -> println("Некорректный ввод! Попробуйте еще раз!")
        }
    }

    private fun handleAdminAuthentication() {
        println("=== Добро пожаловать в Ресторан ===")
        println("1. Регистрация")
        println("2. Вход")
        println("0. Завершение работы программы")

        when (scanner.nextLine()) {
            "1" -> {
                handleAdminRegistration()
            }

            "2" -> {
                handleAdminLogin()
            }

            "0" -> {
                saveData()
                println("Программа завершена.")
                exitProcess(0)
            }

            else -> println("Некорректный ввод! Попробуйте еще раз!")
        }
    }


    private fun handleVisitorRegistration() {
        println("Введите имя пользователя:")
        val username = scanner.nextLine()

        // Ввод пароля дважды для подтверждения
        println("Введите пароль:")
        val password1 = scanner.nextLine()

        println("Повторите ввод пароля:")
        val password2 = scanner.nextLine()

        if (password1 == password2) {
            restaurant.registerVisitor(username, password1)
        } else {
            println("Пароли не совпадают. Регистрация отменена.")
        }
    }

    private fun handleAdminRegistration() {
        println("Введите имя пользователя:")
        val username = scanner.nextLine()

        // Ввод пароля дважды для подтверждения
        println("Введите пароль:")
        val password1 = scanner.nextLine()

        println("Повторите ввод пароля:")
        val password2 = scanner.nextLine()

        if (password1 == password2) {
            restaurant.registerAdmin(username, password1)
        } else {
            println("Пароли не совпадают. Регистрация отменена.")
        }
    }

    private fun handleVisitorLogin() {
        println("Введите имя пользователя:")
        val username = scanner.nextLine()
        println("Введите пароль:")
        val password = scanner.nextLine()
        if (restaurant.loginVisitor(username, password)) {
            currentUser = database.users.find { it.username == username }
            println("Вход выполнен успешно!")
        } else {
            println("Ошибка входа! Проверьте имя пользователя и пароль!")

        }
    }

    private fun handleAdminLogin() {
        println("Введите имя пользователя:")
        val username = scanner.nextLine()
        println("Введите пароль:")
        val password = scanner.nextLine()
        if (restaurant.loginAdmin(username, password)) {
            currentUser = database.users.find { it.username == username }
            println("Вход выполнен успешно!")
        } else {
            println("Ошибка входа! Проверьте имя пользователя и пароль!")

        }
    }

    private fun handleCommandsNone() {
        println("\n=== Команды посетителя ===")
        println("1. Сделать заказ")
        println("2. Посмотреть меню")
        println("0. Выйти из аккаунта")

        when (scanner.nextLine()) {
            "1" -> createOrder()
            "2" -> viewVisitorMenu()
            "0" -> {
                currentUser = null
                println("Выход выполнен успешно!")
            }

            else -> println("Некорректная команда! Попробуйте еще раз!")
        }
    }

    private fun handleCommandsAccepted() {
        println("\n=== Ваши команды ===")
        println("1. Добавить блюдо в заказ")
        println("2. Отправить заказ готовиться")
        println("3. Посмотреть меню")
        println("0. Выйти из аккаунта")

        when (scanner.nextLine()) {
            "1" -> addItemToOrder()
            "2" -> submitOrderForProcessing()
            "3" -> viewVisitorMenu()
            "0" -> {
                currentUser = null
                println("Выход выполнен успешно!")
            }

            else -> println("Некорректная команда! Попробуйте еще раз!")
        }
    }

    private fun handleCommandsPreparing() {
        println("\nВаш заказ готовится!")
        println("1. Отменить заказ")
        println("2. Посмотреть меню")
        println("3. Ждем приготовления")

        when (scanner.nextLine()) {
            "1" -> cancelOrder()
            "2" -> viewVisitorMenu()
            "3" -> println("Ждем приготовления заказа..")
            "0" -> {
                currentUser = null
                println("Выход выполнен успешно!")
            }

            else -> println("Ждем приготовления заказа..")
        }
    }

    private fun handleCommandsReady() {
        println("\nВаш заказ готов!")
        println("1. Оплатить заказ")

        var name = scanner.nextLine()
        while (name != "1") {
            println("\nВаш заказ готов!")
            println("1. Оплатить заказ")
            name = scanner.nextLine()
        }
        payOrder()
    }

    private fun handleCommandsPaid() {
        println("\nСпасибо за заказ!")
        println("1. Оставить отзыв")
        println("0. Выйти из аккаунта")

        when (scanner.nextLine()) {
            "1" -> writeReview()
            "0" -> {
                val visitor = currentUser as Visitor
                visitor.order = null
                currentUser = null
                println("Выход выполнен успешно!")
            }

            else -> println("Некорректная команда! Попробуйте еще раз!")
        }
    }

    private fun handleCommandsOther() {
        val visitor = currentUser as Visitor
        visitor.order = null
        println("\nСпасибо за заказ!")
        println("1. Оформить заказ еще раз")
        println("0. Выйти из аккаунта")

        when (scanner.nextLine()) {
            "1" -> createOrder()
            "0" -> {
                currentUser = null
                println("Выход выполнен успешно!")
            }

            else -> println("Некорректная команда! Попробуйте еще раз!")
        }
    }

    private fun createOrder() {
        restaurant.createOrder(currentUser as Visitor)
    }

    private fun addItemToOrder() {
        viewVisitorMenu()
        println("Введите название блюда из меню:")
        val itemName = scanner.nextLine()
        restaurant.addItemToOrder(currentUser as Visitor, itemName, database.menu)
    }

    private fun cancelOrder() {
        restaurant.cancelOrder(currentUser as Visitor)
    }

    private fun payOrder() {
        val paymentMethod = choosePayment()
        restaurant.payOrder(currentUser as Visitor, paymentMethod)
    }

    private fun choosePayment(): PaymentMethod {
        var paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD
        println("Выберите метод для оплаты! (По умолчанию оплата картой)")
        println("1. Оплатить наличными")
        println("2. Оплатить картой")
        println("3. Оплатить по QR-коду")
        when (scanner.nextLine()) {
            "1" -> paymentMethod = PaymentMethod.CASH
            "2" -> paymentMethod = PaymentMethod.CREDIT_CARD
            "3" -> paymentMethod = PaymentMethod.QR_CODE

            else -> println("Вы выбрали платить картой!")
        }
        return paymentMethod
    }

    private fun submitOrderForProcessing() {
        restaurant.submitOrderForProcessing(currentUser as Visitor)
    }

    private fun writeReview() {
        restaurant.writeReview(currentUser as Visitor)
    }

    private fun handleAdminCommands() {
        println("\n=== Команды админа ===")
        println("1. Добавить новое блюдо в меню")
        println("2. Убрать блюдо из меню")
        println("3. Обновить информацию о блюде из меню")
        println("4. Обновить количество блюда в наличии")
        println("5. Посмотреть меню")
        println("6. Посмотреть статистику по заказам и отзывам")
        println("0. Выйти из аккаунта")

        when (scanner.nextLine()) {
            "1" -> addMenuItem()
            "2" -> removeMenuItem()
            "3" -> updateMenuItem()
            "4" -> updateDishQuantity()
            "5" -> viewAdminMenu()
            "6" -> printStatistics()
            "0" -> {
                currentUser = null
                println("Выход выполнен успешно!")
            }

            else -> println("Некорректная команда! Попробуйте еще раз!")
        }
    }

    private fun addMenuItem() {
        viewAdminMenu()
        println("Введите название нового блюда:")
        val name = scanner.nextLine()
        println("Введите цену блюда:")
        val price = try {
            scanner.nextInt()
        } catch (e: InputMismatchException) {
            println("Ошибка ввода! Введите корректное значение для цены блюда!")
            scanner.nextLine()
            return
        }
        println("Введите время приготовления блюда:")
        val preparationTime = try {
            scanner.nextLong()
        } catch (e: InputMismatchException) {
            println("Ошибка ввода! Введите корректное значение для времени приготовления блюда!")
            scanner.nextLine()
            return
        }
        restaurant.addMenuItem(name, price, preparationTime, database.menu)
    }

    private fun removeMenuItem() {
        viewAdminMenu()
        println("Введите название блюда из меню:")
        val name = scanner.nextLine()
        restaurant.removeMenuItem(name, database.menu)
    }

    private fun updateMenuItem() {
        viewAdminMenu()
        println("Введите название блюда из меню:")
        val name = scanner.nextLine()
        println("Введите новую цену блюда:")
        val price = try {
            scanner.nextInt()
        } catch (e: InputMismatchException) {
            println("Ошибка ввода! Введите корректное значение для цены блюда!")
            scanner.nextLine()
            return
        }
        println("Введите новое время приготовления блюда:")
        val preparationTime = try {
            scanner.nextLong()
        } catch (e: InputMismatchException) {
            println("Ошибка ввода! Введите корректное значение для времени приготовления блюда!")
            scanner.nextLine()
            return
        }
        restaurant.updateMenuItem(name, price, preparationTime, database.menu)
    }

    private fun updateDishQuantity() {
        viewAdminMenu()
        println("Введите название блюда из меню:")
        val name = scanner.nextLine()
        println("Введите новое количества блюда в наличии:")
        val newQuantity = try {
            scanner.nextInt()
        } catch (e: InputMismatchException) {
            println("Ошибка ввода! Введите корректное значение для количества блюда!")
            scanner.nextLine()
            return
        }
        val dish = database.menu.getDishes().find { it.name == name }
        if (dish == null) {
            println("Блюдо $name не найдено в меню!")
        } else {
            restaurant.updateDishQuantity(dish, newQuantity, database.menu)
        }
    }

    private fun viewVisitorMenu() {
        restaurant.viewVisitorMenu()
    }

    private fun viewAdminMenu() {
        restaurant.viewAdminMenu()
    }

    private fun loadData() {
        restaurant.loadData()
    }

    private fun saveData() {
        restaurant.saveData()
    }

    private fun printStatistics() {
        println("\n=== Статистика по заказам и отзывам ===")
        println("1. Самое популярное блюдо")
        println("2. Средний рейтинг для блюд")
        println("3. Общее число заказов и суммарный доход")

        when (scanner.nextLine()) {
            "1" -> restaurant.printMostPopularDishes()
            "2" -> restaurant.printAverageRatingForDishes()
            "3" -> restaurant.printOrdersCountAndRevenue()

            else -> println("Некорректная команда! Попробуйте еще раз!")
        }
    }
}
