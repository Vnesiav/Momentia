package com.example.momentia.Memories

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Memory
import com.example.momentia.R
import com.google.firebase.firestore.FirebaseFirestore

class MemoriesFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var memoriesRecyclerView: RecyclerView
    private lateinit var memoriesAdapter: MemoriesAdapter
    private val memoriesList = mutableListOf<Memory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_memories, container, false)

        db = FirebaseFirestore.getInstance()
        memoriesRecyclerView = view.findViewById(R.id.memoriesRecyclerView)
        memoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        memoriesAdapter = MemoriesAdapter(memoriesList) { memory ->
            sendToFriend(memory)
        }
        memoriesRecyclerView.adapter = memoriesAdapter

        loadMemories()

        val backButton: ImageButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun loadMemories() {
        db.collection("memories")
            .get()
            .addOnSuccessListener { result ->
                memoriesList.clear()
                for (document in result) {
                    try {
                        val memory = document.toObject(Memory::class.java)
                        memoriesList.add(memory)
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error parsing memory: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                memoriesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load memories: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun sendToFriend(memory: Memory) {
        Toast.makeText(
            requireContext(),
            "Sent memory with mediaUrl: ${memory.mediaUrl} to a friend!",
            Toast.LENGTH_SHORT
        ).show()
    }
}
