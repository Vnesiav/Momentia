package com.example.momentia.Friend

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendChat
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendAdapter(
    private val layoutInflater: LayoutInflater,
    private val imageLoader: ImageLoader,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<FriendViewHolder>() {
    private val friends = mutableListOf<FriendChat>()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    fun setData(newFriend: List<FriendChat>) {
        friends.clear()
        friends.addAll(newFriend)
        notifyDataSetChanged()
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val friendToDelete = friends[position]
        AlertDialog.Builder(layoutInflater.context).apply {
            setTitle("Delete Friend")
            setMessage("Are you sure you want to delete ${friendToDelete.firstName} ${friendToDelete.lastName ?: ""}?")
            setPositiveButton("Yes") { _, _ ->
                deleteFriendFromDatabase(position)
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun deleteFriendFromDatabase(position: Int) {
        val friendToDelete = friends[position]
        val friendId = friendToDelete.userId

        // Remove friend from current user's friend list
        db.collection("users")
            .document(currentUser)
            .update("friends", FieldValue.arrayRemove(friendId))
            .addOnSuccessListener {
                // Remove current user from friend's friend list
                db.collection("users")
                    .document(friendId)
                    .update("friends", FieldValue.arrayRemove(currentUser))
                    .addOnSuccessListener {
                        deleteFriend(position) // Update UI
                        Toast.makeText(
                            layoutInflater.context,
                            "Friend deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            layoutInflater.context,
                            "Error removing from friend's list",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(
                    layoutInflater.context,
                    "Error removing friend from your list",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = layoutInflater.inflate(R.layout.friend_list, parent, false)
        return FriendViewHolder(view, imageLoader, object : FriendViewHolder.OnClickListener {
            override fun onItemClick(friend: FriendChat) = onClickListener.onItemClick(friend)
            override fun onDeleteClick(position: Int) = showDeleteConfirmationDialog(position)
        })
    }

    fun deleteFriend(position: Int) {
        if (position in friends.indices) {
            friends.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bindData(friends[position])
    }

    interface OnClickListener {
        fun onItemClick(friend: FriendChat)
    }
}
