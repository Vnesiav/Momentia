import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Memory
import com.example.momentia.Memories.MemoriesAdapter
import com.example.momentia.R
import com.google.firebase.Timestamp
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
        memoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadMemories()

        return view
    }

    private fun loadMemories() {
        db.collection("memories")
            .get()
            .addOnSuccessListener { result ->
                memoriesList.clear()
                for (document in result) {
                    val createdAt = document.getTimestamp("createdAt") ?: Timestamp.now()
                    val mediaUrl = document.getString("mediaUrl") ?: ""
                    val location = document.getString("location") ?: ""

                    val memory = Memory(createdAt, mediaUrl, location)
                    memoriesList.add(memory)
                }

                memoriesAdapter = MemoriesAdapter(memoriesList) { memory ->
                    sendToFriend(memory)
                }
                memoriesRecyclerView.adapter = memoriesAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load memories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendToFriend(memory: Memory) {
        Toast.makeText(requireContext(), "Sent ${memory.mediaUrl} to a friend!", Toast.LENGTH_SHORT).show()
    }
}
