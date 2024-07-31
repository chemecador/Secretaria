package com.chemecador.secretaria.ui.view.friends

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.ActivityFriendsBinding
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class FriendsActivity : AppCompatActivity() {

    private var _binding: ActivityFriendsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        setupNavigation()
    }

    private fun initUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.label_friends)
    }

    private fun setupNavigation() {
        binding.bnvFriends.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_friends -> {
                    loadFragment(FriendListFragment())
                }

                R.id.menu_friend_requests -> {
                    loadFragment(FriendRequestsFragment())
                }

                R.id.menu_add_friend -> {
                    loadFragment(AddFriendFragment())
                }
            }
            true
        }
        // Load the default fragment
        binding.bnvFriends.selectedItemId = R.id.menu_friends
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
