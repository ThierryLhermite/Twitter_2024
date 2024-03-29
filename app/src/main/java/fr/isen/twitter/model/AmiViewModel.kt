package fr.isen.twitter.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AmiViewModel : ViewModel() {
    private val _friendRequests = MutableLiveData<List<String>>()
    val friendRequests: LiveData<List<String>> = _friendRequests

    fun loadFriendRequests(uid: String) {
        val friendRequestsRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$uid/friendRequests")

        friendRequestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children.mapNotNull { it.key }
                _friendRequests.value = requests
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AmiViewModel", "Erreur lors du chargement des demandes d'ami", error.toException())
            }
        })
    }

    fun fetchUsername(uid: String, onResult: (String) -> Unit) {

        Log.d("AmiViewModel", "fetchUsername appelé avec l'UID: $uid")
        val userRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$uid/username")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java) ?: "Utilisateur inconnu"
                onResult(username)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AmiViewModel", "Failed to read username.", error.toException())
                onResult("Erreur de chargement")
            }
        })
    }

}
