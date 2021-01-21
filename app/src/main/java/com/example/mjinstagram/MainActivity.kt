package com.example.mjinstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mjinstagram.navigation.account.AccountFragment
import com.example.mjinstagram.navigation.home.HomeFragment
import com.example.mjinstagram.navigation.notice.NotificationsFragment
import com.example.mjinstagram.navigation.photo.PhotoActivity
import com.example.mjinstagram.navigation.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav_view.setOnNavigationItemSelectedListener(this)
        nav_view.selectedItemId = R.id.navigation_home

        var actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        // 앨범 접근 권한 요청
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {

                val homeFragment = HomeFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, homeFragment)
                        .commit()
                return true
            }
            R.id.navigation_search -> {
                val searchFragment = SearchFragment()
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, searchFragment)
                        .commit()
                return true
            }
            R.id.navigation_photo -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(Intent(this, PhotoActivity::class.java))
                } else {
                    Toast.makeText(this, "스토리지 읽기 권한이 없습니다.", Toast.LENGTH_LONG).show()
                }
                return true
            }
            R.id.navigation_notifications -> {
                val notificationsFragment = NotificationsFragment()
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, notificationsFragment)
                        .commit()
                return true
            }
            R.id.navigation_account -> {
                val accountFragment = AccountFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, accountFragment)
                        .commit()
                return true
            }
        }
        return false
    }
}