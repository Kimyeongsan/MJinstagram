package com.example.mjinstagram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    // Firebase Authentication 관리 클래스
    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase 로그인 통합 관리하는 Object 만들기
        auth = FirebaseAuth.getInstance()

        //이메일 로그인 세팅
        sign_in_btn.setOnClickListener {
            signInAndSignUp()
        }
    }

    //이메일 회원가입 및 로그인 메소드
    fun signInAndSignUp() {

        auth?.createUserWithEmailAndPassword(login_email.text.toString(), login_password.text.toString())
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //아이디 생성이 성공했을 경우
                        Toast.makeText(this,
                                getString(R.string.signup_complete), Toast.LENGTH_SHORT).show()

                        //다음페이지 호출
                        moveMainPage(auth?.currentUser)
                    } else if (!task.exception?.message.isNullOrEmpty()) {
                        //회원가입 에러가 발생했을 경우
                        Toast.makeText(this,
                                task.exception!!.message, Toast.LENGTH_SHORT).show()
                    } else {
                        //아이디 생성도 안되고 에러도 발생되지 않았을 경우 로그인
                        signinEmail()
                    }
                }
    }

    //로그인 메소드
    fun signinEmail() {

        auth?.createUserWithEmailAndPassword(login_email.text.toString(), login_password.text.toString())
                ?.addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        //로그인 성공 및 다음페이지 호출
                        moveMainPage(auth?.currentUser)
                    } else {
                        //로그인 실패
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }

    }

    fun moveMainPage(user: FirebaseUser?) {

        // User is signed in
        if (user != null) {
            Toast.makeText(this, getString(R.string.signin_complete), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

//    override fun onStart() {
//        super.onStart()
//        //자동 로그인 설정
//        moveMainPage(auth?.currentUser)
//    }
}