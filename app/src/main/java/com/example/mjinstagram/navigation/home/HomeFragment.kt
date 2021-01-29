package com.example.mjinstagram.navigation.home

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils.replace
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mjinstagram.MainActivity
import com.example.mjinstagram.R
import com.example.mjinstagram.data.AlarmDTO
import com.example.mjinstagram.data.ContentDTO
import com.example.mjinstagram.data.FollowDTO
import com.example.mjinstagram.navigation.account.AccountFragment
import com.example.mjinstagram.utill.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.okhttp.OkHttpClient

import kotlinx.android.synthetic.main.acitivity_add_photo.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_home.view.*
import java.util.ArrayList

class HomeFragment : Fragment() {

    var firestore: FirebaseFirestore? = null

    var uid : String? = null

    var mainview: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainview = LayoutInflater.from(activity).inflate(R.layout.fragment_home, container, false)

        firestore = FirebaseFirestore.getInstance()

        uid = FirebaseAuth.getInstance().currentUser?.uid

        mainview?.home_recyclers?.adapter = HomeRecyclerViewAdapter()
        mainview?.home_recyclers?.layoutManager = LinearLayoutManager(activity)

        return mainview
    }


    inner class HomeRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        val contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()

                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)!!
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView

            // 유저 아이디
            viewHolder.detailviewitem_profile_textview.text = contentDTOs[position].userId

            // 가운데 이미지
            Glide.with(holder.itemView.context)
                    .load(contentDTOs[position].imageUrl)
                    .into(viewHolder.detailviewitem_imageview_content)

            // 설명 텍스트
            viewHolder.detailviewitem_explain_textview.text = contentDTOs[position].explain

            //좋아요 카운터 설정
            viewHolder.detailviewitem_favoritecounter_textview.text = "좋아요 " + contentDTOs[position].favoriteCount

            // 프로필 이미지
            Glide.with(holder.itemView.context)
                    .load(contentDTOs[position].imageUrl)
                    .into(viewHolder.detailviewitem_profile_image)

            // 좋아요 이벤트
            viewHolder.detailviewitem_favorite_imageview.setOnClickListener { favoriteEvent(position) }

            //좋아요 버튼 설정
            if (contentDTOs[position].favorites.containsKey(FirebaseAuth.getInstance().currentUser!!.uid)) {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)

            } else {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            //UserFragment로 이동
            viewHolder.detailviewitem_profile_image.setOnClickListener {

                val fragment = AccountFragment()
                val bundle = Bundle()

                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)

                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.nav_view, fragment)?.commit()
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        //좋아요 이벤트 기능
        private fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    // Unstar the post and remove self from stars
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                    contentDTO?.favorites.remove(uid)

                } else {
                    // Star the post and add self to stars
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                    contentDTO?.favorites[uid] = true
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

    }
}