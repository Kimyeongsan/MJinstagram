package com.example.mjinstagram.navigation.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
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
import com.example.mjinstagram.data.AlarmDTO
import com.example.mjinstagram.data.ContentDTO
import com.example.mjinstagram.data.FollowDTO
import com.example.mjinstagram.navigation.home.HomeFragment
import com.example.mjinstagram.utill.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList
import kotlinx.android.synthetic.main.fragment_account.view.*

class AccountFragment : Fragment() {
    var root : View? = null

    var uid: String? = null
    var currentUserUid: String? = null

    // Firebase
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    var fcmPush: FcmPush? = null


    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        root = inflater.inflate(R.layout.fragment_account, container, false)

        uid = arguments?.getString("destinationUid")
        currentUserUid = auth?.currentUser?.uid

        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        fcmPush = FcmPush()

        root?.account_recyclerview?.adapter = AccountFragmentRecyclerViewAdapter()
        root?.account_recyclerview?.layoutManager = GridLayoutManager(activity, 3)

        // Profile Image Click Listener
        root?.account_iv_profile?.setOnClickListener {
            //앨범 오픈
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }

        // User 비교 문
        userCompare()

        // 프로필 이미지 업로드
        getProfileImage()

        //Follower 카운트
        getFollower()

        return root
    }

    fun userCompare() {

            // 본인 계정인 경우 -> 로그아웃, Toolbar 기본으로 설정
            if (uid == currentUserUid) {

                root?.account_btn_follow_signout?.text = getString(R.string.signout)
                root?.account_btn_follow_signout?.setOnClickListener {
                    activity?.finish()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    auth?.signOut()
                }
            } else {
                root?.account_btn_follow_signout?.text = getString(R.string.follow)

                var mainActivity = (activity as MainActivity)

                mainActivity.toolbar_title_image.visibility = View.GONE
                mainActivity.toolbar_btn_back.visibility = View.VISIBLE
                mainActivity.toolbar_username.visibility = View.VISIBLE

                mainActivity.toolbar_username.text = arguments?.getString("userId")
                mainActivity.toolbar_btn_back.setOnClickListener {
                    mainActivity.bottom_nav.selectedItemId = R.id.navigation_home
                }

                root?.account_btn_follow_signout?.setOnClickListener {
                    requestFollow()
                }
            }

    }

    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                    if (documentSnapshot?.data != null) {
                        var url = documentSnapshot?.data!!["image"]
//                        activity!! 안먹음
                        Glide.with(requireActivity())
                                .load(url)
                                .apply(RequestOptions().circleCrop()).into(root?.account_iv_profile!!)
                    }
                }

    }

    fun getFollower() {
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot == null) return@addSnapshotListener

            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)

            if(followDTO?.followingCount != null){
                root?.account_tv_following_count?.text = followDTO?.followingCount.toString()
            }

            if(followDTO?.followerCount != null){
                root?.account_tv_follower_count?.text = followDTO?.followerCount.toString()

                if (followDTO?.followers?.containsKey(currentUserUid)!!) {

                    root?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                    //activity!! 안먹음
                    root?.account_btn_follow_signout?.background?.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
                } else {
                    if (uid != currentUserUid) {
                        root?.account_btn_follow_signout?.text = getString(R.string.follow)
                        root?.account_btn_follow_signout?.background?.colorFilter = null
                    }
                }
            }
        }
    }

    fun requestFollow() {

        var tsDocFollowing = firestore!!.collection("users").document(currentUserUid!!)
        firestore?.runTransaction { transaction ->

            var followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)
            if (followDTO == null) {

                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO.followings[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction

            }
            // Unstar the post and remove self from stars
            if (followDTO?.followings?.containsKey(uid)!!) {

                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followings.remove(uid)
            } else {

                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followings[uid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore!!.collection("users").document(uid!!)
        firestore?.runTransaction { transaction ->

            var followDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)
            if (followDTO == null) {

                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true


                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if (followDTO?.followers?.containsKey(currentUserUid!!)!!) {


                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)
            } else {

                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true

            }// Star the post and add self to stars

            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }

    }

    fun followerAlarm(destinationUid: String) {

        val alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser!!.email
        alarmDTO.uid = auth?.currentUser!!.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()

        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        var message = auth?.currentUser!!.email + getString(R.string.alarm_follow)
        fcmPush?.sendMessage(destinationUid, "알림 메세지 입니다.", message)
    }

    inner class AccountFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        init {
            // 나의 사진만 찾기
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                if (querySnapshot == null) return@addSnapshotListener
                for (snapshot in querySnapshot.documents) {
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