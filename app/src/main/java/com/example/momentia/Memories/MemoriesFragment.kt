package com.example.momentia.Memories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Memory
import com.example.momentia.DTO.MemorySection
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class MemoriesFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var memoriesRecyclerView: RecyclerView
    private lateinit var memoriesAdapter: MemoriesAdapter
    private val memoriesList = mutableListOf<Memory>()
    private val filteredMemoriesList = mutableListOf<Memory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_memories, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        memoriesRecyclerView = view.findViewById(R.id.memoriesRecyclerView)

        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (memoriesAdapter.getItemViewType(position)) {
                    MemoriesAdapter.VIEW_TYPE_HEADER -> gridLayoutManager.spanCount
                    MemoriesAdapter.VIEW_TYPE_ITEM -> 1
                    else -> 1
                }
            }
        }
        memoriesRecyclerView.layoutManager = gridLayoutManager

        val initialSections = organizeMemoriesByDate(filteredMemoriesList)
        memoriesAdapter = MemoriesAdapter(initialSections)
        memoriesRecyclerView.adapter = memoriesAdapter

        val searchView: androidx.appcompat.widget.SearchView = view.findViewById(R.id.search_date)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterMemories(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMemories(newText)
                return true
            }
        })

        loadMemories()

        val backButton: ImageButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun organizeMemoriesByDate(memories: List<Memory>): List<MemorySection> {
        val sections = mutableListOf<MemorySection>()
        var lastDate: String? = null

        memories.sortedByDescending { memory ->
            memory.sentAt?.toDate()
        }.forEach { memory ->
            val currentDate = memory.sentAt?.let {
                val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                dateFormat.format(it.toDate())
            }

            if (currentDate != lastDate) {
                sections.add(MemorySection.Header(memory))
                lastDate = currentDate
            }
            sections.add(MemorySection.Item(memory))
        }
        return sections
    }

    private fun loadMemories() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("Auth Error", "User not logged in")
            return
        }

        val senderId = currentUser.uid
        db.collection("memories")
            .whereEqualTo("senderId", senderId)
            .get()
            .addOnSuccessListener { result ->
                memoriesList.clear()
                for (document in result) {
                    try {
                        val memory = document.toObject(Memory::class.java)
                        memoriesList.add(memory)
                    } catch (e: Exception) {
                        android.util.Log.e("Firestore Error", "Failed to parse memory", e)
                    }
                }

                val organizedSections = organizeMemoriesByDate(memoriesList)
                memoriesAdapter = MemoriesAdapter(organizedSections)
                memoriesRecyclerView.adapter = memoriesAdapter
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore Error", "Failed to load memories", e)
            }
    }

    private fun filterMemories(query: String?) {
        filteredMemoriesList.clear()
        if (query.isNullOrEmpty()) {
            filteredMemoriesList.addAll(memoriesList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())

            filteredMemoriesList.addAll(
                memoriesList.filter {
                    val formattedDate = it.sentAt?.let { timestamp ->
                        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        dateFormat.format(timestamp.toDate())
                    } ?: "Unknown Date"

                    formattedDate.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
                }
            )
        }

        val organizedSections = organizeMemoriesByDate(filteredMemoriesList)
        memoriesAdapter = MemoriesAdapter(organizedSections)
        memoriesRecyclerView.adapter = memoriesAdapter
    }
}
