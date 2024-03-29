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

    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> = _friends

    fun loadFriends(uid: String) {
        val friendsRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$uid/friends")

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friends = snapshot.children.mapNotNull { it.key }
                _friends.value = friends
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AmiViewModel", "Erreur lors du chargement des amis", error.toException())
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

    fun acceptFriend(currentUid: String, targetUid: String) {
        val database = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
        val friendRequestsRef = database.getReference("Users/$currentUid/friendRequests/$targetUid")
        val friendsRef = database.getReference("Users/$currentUid/friends/$targetUid")

        // Supprime l'UID de la liste des demandes d'ami
        friendRequestsRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("acceptFriend", "Demande d'ami supprimée avec succès.")

                // Ajoute cet UID à la liste des amis
                friendsRef.setValue(true).addOnCompleteListener { friendTask ->
                    if (friendTask.isSuccessful) {
                        Log.d("acceptFriend", "Ami ajouté avec succès.")
                    } else {
                        Log.e("acceptFriend", "Erreur lors de l'ajout de l'ami", friendTask.exception)
                    }
                }
            } else {
                Log.e("acceptFriend", "Erreur lors de la suppression de la demande d'ami", task.exception)
            }
        }
        loadFriendRequests(currentUid)
    }

    fun refuseFriend(currentUid: String, targetUid: String) {
        val database = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
        val friendRequestsRef = database.getReference("Users/$currentUid/friendRequests/$targetUid")

        // Supprime l'UID de la liste des demandes d'ami
        friendRequestsRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("refuseFriend", "Demande d'ami refusée et supprimée avec succès.")
            } else {
                Log.e("refuseFriend", "Erreur lors de la suppression de la demande d'ami", task.exception)
            }
        }
        loadFriendRequests(currentUid)
    }


}
