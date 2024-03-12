import kotlinx.serialization.Serializable

@Serializable
class Dish(val name: String, var price: Int, var cookingTime: Long, var quantity: Int) {
    val reviews: MutableList<Review> = mutableListOf()
    override fun toString(): String {
        return "$name-цена:$price-количество:$quantity"
    }
}

@Serializable
class Menu {
    private val dishes: MutableSet<Dish> = mutableSetOf()

    fun addDish(dish: Dish) {
        dishes.add(dish)
    }

    fun removeDish(dish: Dish) {
        dishes.remove(dish)
    }

    fun updateDishQuantity(dish: Dish, newQuantity: Int) {
        dish.quantity = newQuantity
    }

    fun getDishes(): MutableSet<Dish> {
        return dishes
    }
}
