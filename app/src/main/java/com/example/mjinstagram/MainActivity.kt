package com.example.mjinstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        bottom_navigation()
        bottom_nav.selectedItemId = R.id.navigation_home

        // 앨범 접근 권한 요청
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

    }

    fun bottom_navigation() {
        bottom_nav.setOnNavigationItemSelectedListener {

            setToolbarDefault()

            when (it.itemId) {
                R.id.navigation_home -> {

                    val homeFragment = HomeFragment()
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_view, homeFragment)
                            .commit()
                    true
                }

                R.id.navigation_search -> {
                    val searchFragment = SearchFragment()
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.nav_view, searchFragment)
                            .commit()
                    true
                }

                R.id.navigation_photo -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(Intent(this, PhotoActivity::class.java))
                    } else {
                        Toast.makeText(this, "스토리지 읽기 권한이 없습니다.", Toast.LENGTH_LONG).show()
                    }
                    true
                }

                R.id.navigation_notifications -> {
                    val notificationsFragment = NotificationsFragment()
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_view, notificationsFragment)
                            .commit()
                    true
                }

                R.id.navigation_account -> {
                    val accountFragment = AccountFragment()
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val bundle = Bundle()
                    bundle.putString("destinationUid", uid)
                    accountFragment.arguments = bundle
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.nav_view, accountFragment)
                            .commit()
                    true
                }
                else -> false
            }
        }
    }

    fun setToolbarDefault() {
        toolbar_title_image.visibility = View.VISIBLE
        toolbar_btn_back.visibility = View.GONE
        toolbar_username.visibility = View.GONE
    }
}