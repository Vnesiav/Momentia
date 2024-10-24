package com.example.momentia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class FriendFragment : Fragment() {

    private lateinit var profileImageButton: ImageButton
    private lateinit var editProfileButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the ImageButton by its ID
        profileImageButton = view.findViewById(R.id.imageButton)
        editProfileButton = view.findViewById(R.id.editProfileButton)

        // Set OnClickListener for the profileImageButton (if needed)
//        profileImageButton.setOnClickListener {
//            findNavController().navigate(R.id.action_friendFragment_to_profileFragment)
//        }

        // Set OnClickListener for the editProfileButton to navigate to EditProfileFragment
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_friendFragment_to_editProfileFragment)
        }
    }
}
