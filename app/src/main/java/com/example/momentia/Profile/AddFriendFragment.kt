package com.example.momentia.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.R

class AddFriendFragment : BaseAuthFragment() {
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)
        backButton = view.findViewById(R.id.back_button)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }


}