package com.example.sportproapp.data.remote

import com.example.sportproapp.data.model.TeamModel
import com.example.sportproapp.data.model.User
import com.example.sportproapp.data.model.UserRole
import com.example.sportproapp.data.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(name: String, email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid?: throw Exception("User ID is null")

            val user = User(uid, name, email, UserRole.JUG, UserStatus.ACTIVE)

            firestore.collection("users").document(uid).set(user).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit>{

        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun registerTeam(teamName: String, category: String, entrenador: String): Result<Unit> {

        val team = TeamModel(name = teamName, category = category, coachName = entrenador )

        return try {
            val docRef = firestore.collection("teams").document()
            val teamtoSave = team.copy(id = docRef.id)

            docRef.set(teamtoSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}