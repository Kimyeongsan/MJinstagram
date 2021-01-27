package com.example.mjinstagram.navigation.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mjinstagram.LoginActivity
import com.example.mjinstagram.MainActivity
import com.example.mjinstagram.R
import com.example.mjinstagram.data.ContentDTO
import com.example.mjinstagram.navigation.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList
import kotlinx.android.synthetic.main.fragment_account.view.*

class AccountFragment : Fragment() {
    var root : View ?= null

    var uid: String? = null
    var currentUserUid: String? = null

    // Firebase
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    val PICK_PROFILE_FROM_ALBUM = 10

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        root = inflater.inflate(R.layout.fragment_account, container, false)

        uid = arguments?.getString("destinationUid")
        currentUserUid = auth?.currentUser?.uid

        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        root?.account_recyclerview?.adapter = AccountFragmentRecyclerViewAdapter()
        root?.account_recyclerview?.layoutManager = GridLayoutManager(requireActivity(), 3)

        // User 비교 문
        userCompare()

        // 프로필 이미지 업로드
        getProfileImage()

        return root
    }

    fun userCompare() {


            // 본인 계정인 경우 -> 로그아웃, Toolbar 기본으로 설정
            if (uid != null && uid == currentUserUid) {

                root!!.account_btn_follow_signout.text = getString(R.string.signout)
                root?.account_btn_follow_signout?.setOnClickListener {
                    startActivity(Intent(activity, LoginActivity::class.java))
                    activity?.finish()
                    auth?.signOut()
                }
            } else {
                root!!.account_btn_follow_signout.text = getString(R.string.follow)

                var mainActivity = (activity as MainActivity)
                mainActivity.toolbar_username.text = requireArguments().getString("userId")
                mainActivity.toolbar_btn_back.setOnClickListener {
                    mainActivity.bottom_nav.selectedItemId = R.id.navigation_home
                }

                mainActivity.toolbar_title_image.visibility = View.GONE
                mainActivity.toolbar_btn_back.visibility = View.VISIBLE
                mainActivity.toolbar_username.visibility = View.VISIBLE

//                root?.account_btn_follow_signout?.setOnClickListener {
//                    requestFollow()
//                }

                // Profile Image Click Listener
                root?.account_iv_profile?.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //앨범 오픈
                        var photoPickerIntent = Intent(Intent.ACTION_PICK)
                        photoPickerIntent.type = "image/*"
                        activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
                    }
                }
            }

    }

    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if(documentSnapshot == null)  return@addSnapshotListener
                    if(documentSnapshot.data != null) {
                        val url = documentSnapshot?.data!!["image"]
                        Glide.with(requireActivity())
                                .load(url)
                                .apply(RequestOptions().circleCrop()).into(root!!.account_iv_profile)
                    }
                }

    }

    inner class AccountFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        init {
            // 나의 사진만 찾기
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                if (querySnapshot == null) return@addSnapshotListener
                for (snapshot in querySnapshot?.documents!!) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }

                root?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged()

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3

            val imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)

            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context)
                    .load(contentDTOs[position].imageUrl)
                    .apply(RequestOptions().centerCrop())
                    .into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}