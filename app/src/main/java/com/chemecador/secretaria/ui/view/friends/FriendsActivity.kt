package com.chemecador.secretaria.ui.view.friends

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.ActivityFriendsBinding
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsActivity : AppCompatActivity() {

    private var _binding: ActivityFriendsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FriendsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()

    }

    private fun initUI() {

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setTitle(R.string.label_friends)

        binding.bnvFriends.selectedItemId = R.id.menu_friends

        binding.bnvFriends.setOnItemSelectedListener { item: MenuItem ->
            setupFragment(item)
            item.isChecked = true
            true
        }
    }

    private fun setupFragment(item: MenuItem) {
        var fragment: Fragment? = null
        when (item.itemId) {
            R.id.menu_friends -> {
                fragment = FriendListFragment()
            }

            R.id.menu_friend_requests -> {
                //fragment = FriendRequestFragment()
            }

            R.id.menu_add_friend -> {
                fragment = AddFriendFragment()
            }
        }
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
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