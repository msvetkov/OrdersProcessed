package com.lotuss.ordersisprocessed.waiter.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lotuss.ordersisprocessed.R
import com.lotuss.ordersisprocessed.data.food.Food
import kotlinx.android.synthetic.main.menu_food_item.view.*

class MenuFoodAdapter(private val layoutInflater: LayoutInflater, private val items: MutableList<Food>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.menu_food_item, parent, false)
        return FoodHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val foodHolder: FoodHolder = holder as FoodHolder
        val food = items[position]
        foodHolder.title.text = food.title
        foodHolder.count.text = food.count.toString()

        foodHolder.minus.setOnClickListener{
            minus(food, foodHolder)
        }
        foodHolder.plus.setOnClickListener {
            plus(food, foodHolder)
        }
    }

    fun minus(food: Food, foodHolder: FoodHolder){
        if (food.count != 0) {
            food.count--
            foodHolder.count.text = food.count.toString()
        }
    }

    fun plus(food: Food, foodHolder: FoodHolder){
        food.count++
        foodHolder.count.text = food.count.toString()
    }

    class FoodHolder(view: View): RecyclerView.ViewHolder(view){
        val title: TextView = view.title
        val count: TextView = view.count
        val minus: Button = view.minus
        val plus: Button = view.plus
    }
}