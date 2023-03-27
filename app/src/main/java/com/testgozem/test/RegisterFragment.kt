package com.testgozem.test

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.testgozem.R


class RegisterFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.register_ui, container, false)
        val auth = FirebaseAuth.getInstance()
        val btnRegister = rootView.findViewById<Button>(R.id.btn_register)
        val emailEditText = rootView.findViewById<EditText>(R.id.email_edit_text)
        val fullNameEditText= rootView.findViewById<EditText>(R.id.fullname_edit_text)
        val passwordEditText = rootView.findViewById<EditText>(R.id.password_edit_text)
        val loginText = rootView.findViewById<TextView>(R.id.login_txt)
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        loginText.setOnClickListener{
            findNavController().navigate(R.id.loginFragment)
        }



        btnRegister.setOnClickListener {
            progressDialog.show()
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && fullName.isNotEmpty())
            {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build()
                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                                progressDialog.dismiss()
                                if (updateTask.isSuccessful) {
                                    findNavController().navigate(R.id.homeFragment)
                                } else {
                                    Toast.makeText(requireContext(), "Authentication failed.Password must have at least 6 characters",
                                        Toast.LENGTH_SHORT).show()
                                }

                            }
                        } else {
                            Toast.makeText(requireContext(), "Authentication failed.Password must have at least 6 characters",
                                Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    }
            }
            else
            {
                Toast.makeText(requireContext(), "All fields are obligatory",
                    Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }


        }
        return rootView
    }
}


