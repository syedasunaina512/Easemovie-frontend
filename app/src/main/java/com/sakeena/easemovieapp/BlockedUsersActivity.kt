package com.sakeena.easemovieapp

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BlockedUsersActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyLayout: View
    private lateinit var adapter: BlockedAdapter

    // 🔥 Start with EMPTY list (real behavior)
    private val userList = mutableListOf<BlockedUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_users)

        recycler = findViewById(R.id.recyclerBlocked)
        emptyLayout = findViewById(R.id.layoutEmpty)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        setupRecycler()
        checkEmpty()

        btnBack.setOnClickListener {
            finish()
        }

        // 🔥 TEST (remove later)
        // addDummyUsers()
    }

    private fun setupRecycler() {
        adapter = BlockedAdapter(userList) { position ->
            userList.removeAt(position)
            adapter.notifyItemRemoved(position)
            checkEmpty()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun checkEmpty() {
        if (userList.isEmpty()) {
            recycler.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            recycler.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
        }
    }

    // 🔥 OPTIONAL: Dummy data for testing UI
    private fun addDummyUsers() {
        userList.add(BlockedUser("Ali"))
        userList.add(BlockedUser("Ahmed"))
        userList.add(BlockedUser("Sara"))

        adapter.notifyDataSetChanged()
        checkEmpty()
    }
}