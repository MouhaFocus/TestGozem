package com.testgozem.test

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.testgozem.R


class LogInFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.login_ui, container, false)
        val auth = FirebaseAuth.getInstance()
        val btnLogin = rootView.findViewById<Button>(R.id.login_btn)
        val txtRegister = rootView.findViewById<TextView>(R.id.register_txt)
        val emailEditText = rootView.findViewById<EditText>(R.id.mail)
        val passwordEditText = rootView.findViewById<EditText>(R.id.pwd)
        val progressDialog = ProgressDialog(context)

        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

            btnLogin.setOnClickListener{


                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                progressDialog.show()

                if (email.isNotEmpty() && password.isNotEmpty())
                {

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                findNavController().navigate(R.id.homeFragment)
                            } else {
                                Toast.makeText(requireContext(), "Authentication failed.Please Register",
                                    Toast.LENGTH_SHORT).show()
                            }
                            progressDialog.dismiss()
                        }
                }
                else
                {
                    Toast.makeText(requireContext(), "All fields are obligatory",
                        Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }


            }


        txtRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }




        return rootView
    }


}