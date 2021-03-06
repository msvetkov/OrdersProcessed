package com.lotuss.ordersisprocessed.cook

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lotuss.ordersisprocessed.AuthActivity
import com.lotuss.ordersisprocessed.R
import com.lotuss.ordersisprocessed.data.orders.Order
import kotlinx.android.synthetic.main.activity_cook.*

class CookActivity : AppCompatActivity() {

    private var orderList: MutableList<Order> = mutableListOf()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cook)
        supportActionBar?.title = applicationContext.getString(R.string.title_orders)
        val progressBar: ProgressBar = this.progress_circular
        val recyclerView: RecyclerView = this.recycler_orders
        val adapter = CookOrdersAdapter(layoutInflater, orderList)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
        getOrderList(progressBar, adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id){
            R.id.action_exit -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun sendNotification(order: Order){
        val notificationManager: NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifyId = 0
        val id = applicationContext.getString(R.string.default_notification_channel_id)
        val title = applicationContext.getString(R.string.default_notification_channel_title)
        val intent: Intent
        val pendingIntent: PendingIntent
        val builder: NotificationCompat.Builder
        var notificationTitle = ""
        var notificationMessage = ""
        when (order.status){
            0 -> {
                notificationTitle = applicationContext.getString(R.string.new_order_title)
                notificationMessage = applicationContext.getString(R.string.new_order_message, order.id)
            }
            3 -> {
                notificationTitle = applicationContext.getString(R.string.order_is_cancelled_title)
                notificationMessage = applicationContext.getString(R.string.order_is_cancelled_message, order.id)
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            var mChannel: NotificationChannel? = notificationManager.getNotificationChannel(id)
            if (mChannel == null) {
                mChannel = NotificationChannel(id, title, importance)
                mChannel.enableVibration(true)
                mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                notificationManager.createNotificationChannel(mChannel)
            }
            builder = NotificationCompat.Builder(applicationContext, id)
            intent = Intent(applicationContext, CookActivity::class.java)
            pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            builder.setContentTitle(notificationTitle)
                    .setSmallIcon(R.drawable.splash_for_v23)
                    .setContentText(notificationMessage)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(100, 200, 400, 300, 500, 400, 300, 200, 400))
        }else{
            builder = NotificationCompat.Builder(applicationContext, id)
            intent = Intent(applicationContext, CookActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            builder.setContentTitle(notificationTitle)
                    .setSmallIcon(R.drawable.splash_for_v23)
                    .setContentText(notificationMessage)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
        }
        val notification = builder.build()
        notificationManager.notify(notifyId, notification)
    }


    private fun getOrderList(progressBar: ProgressBar, adapter: CookOrdersAdapter){
        val orderReference = database.getReference("orders")
        orderReference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressBar.visibility = View.VISIBLE
                orderList.clear()
                for (ds in dataSnapshot.children) {
                    val order: Order = ds.getValue(Order::class.java)!!
                    orderList.add(order)
                    adapter.notifyDataSetChanged()
                    if (!order.checkedByCook)
                    {
                        sendNotification(order)
                        orderReference.child(order.id.toString()).child("checkedByCook").setValue(true)
                    }
                }
                progressBar.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
        })
    }
}
