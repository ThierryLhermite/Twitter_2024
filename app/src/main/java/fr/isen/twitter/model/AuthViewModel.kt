package fr.isen.twitter.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData pour observer les changements d'état de l'utilisateur
    val userLiveData = MutableLiveData<FirebaseUser?>()

    fun register(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                userLiveData.postValue(currentUser)

                // Ajoutez des informations supplémentaires dans la base de données
                currentUser?.let { user ->
                    val userId = user.uid
                    val userMap = mapOf(
                        "username" to username,
                        "email" to email,
                        "friends" to emptyMap<String, Boolean>() // Utilisez une Map vide pour la future liste d'amis
                        // Ajoutez ici d'autres informations comme "age" si nécessaire
                    )

                    // Spécifiez le chemin où vous voulez sauvegarder les données dans votre base de données
                    val databaseReference = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
                        .getReference("Users/$userId")

                    databaseReference.setValue(userMap).addOnCompleteListener { dbTask ->
                        if (!dbTask.isSuccessful) {
                            // Gérez l'erreur de sauvegarde dans la base de données, si nécessaire
                            Log.e("AuthViewModel", "Erreur de sauvegarde des données utilisateur", dbTask.exception)
                        }
                    }
                }
            } else {
                userLiveData.postValue(null)
            }
        }
    }

    fun addFriend(userId: String, friendId: String) {
        // Chemin vers la liste d'amis de l'utilisateur dans la base de données
        val friendListRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$userId/friends")

        // Ajouter l'UID de l'ami avec une valeur 'true' pour le marquer comme ami
        friendListRef.child(friendId).setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AuthViewModel", "Ami ajouté avec succès")
            } else {
                Log.e("AuthViewModel", "Erreur lors de l'ajout d'un ami", task.exception)
            }
        }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userLiveData.postValue(auth.currentUser)
                } else {
                    userLiveData.postValue(null)
                }
            }
    }

    fun signOut() {
        auth.signOut()
        userLiveData.postValue(null)
    }
}
