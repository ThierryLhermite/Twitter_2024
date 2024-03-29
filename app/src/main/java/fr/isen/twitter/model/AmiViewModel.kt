package fr.isen.twitter.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AmiViewModel : ViewModel() {
    private val _friendRequests = MutableLiveData<List<String>>()
    val friendRequests: LiveData<List<String>> = _friendRequests
    private val _friendsCount = MutableLiveData<Int>()
    val friendsCount: LiveData<Int> = _friendsCount

    fun removeFriend(friendUid: String, onComplete: () -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid ?: return

        // Chemin de référence pour supprimer l'ami de la liste de l'utilisateur actuel
        val friendRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$currentUid/friends/$friendUid")

        friendRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AmiViewModel", "Ami supprimé avec succès.")
                onComplete()
            } else {
                Log.e("AmiViewModel", "Erreur lors de la suppression de l'ami.", task.exception)
            }
        }
    }

    fun loadFriendsCount(uid: String) {
        val friendsRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$uid/friends")

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Le compte d'amis est égal au nombre d'enfants sous le noeud `friends`
                val count = snapshot.childrenCount.toInt()
                _friendsCount.value = count
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AmiViewModel", "Erreur lors du chargement du compte d'amis", error.toException())
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

    fun acceptFriend(currentUid: String, targetUid: String) {
        val database = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
        val friendRequestsRefCurrent = database.getReference("Users/$currentUid/friendRequests/$targetUid")
        val friendsRefCurrent = database.getReference("Users/$currentUid/friends/$targetUid")
        val friendsRefTarget = database.getReference("Users/$targetUid/friends/$currentUid")

        // Supprime l'UID de la liste des demandes d'ami pour l'utilisateur actuel
        friendRequestsRefCurrent.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("acceptFriend", "Demande d'ami supprimée avec succès pour l'utilisateur actuel.")

                // Ajoute cet UID à la liste des amis de l'utilisateur actuel
                friendsRefCurrent.setValue(true).addOnCompleteListener { friendTask ->
                    if (friendTask.isSuccessful) {
                        Log.d("acceptFriend", "Ami ajouté avec succès à la liste des amis de l'utilisateur actuel.")

                        // Ajoute l'UID de l'utilisateur actuel à la liste des amis de l'utilisateur cible
                        friendsRefTarget.setValue(true).addOnCompleteListener { targetFriendTask ->
                            if (targetFriendTask.isSuccessful) {
                                Log.d("acceptFriend", "Utilisateur actuel ajouté avec succès à la liste des amis de l'utilisateur cible.")
                            } else {
                                Log.e("acceptFriend", "Erreur lors de l'ajout de l'utilisateur actuel à la liste des amis de l'utilisateur cible", targetFriendTask.exception)
                            }
                        }
                    } else {
                        Log.e("acceptFriend", "Erreur lors de l'ajout de l'ami à la liste des amis de l'utilisateur actuel", friendTask.exception)
                    }
                }
            } else {
                Log.e("acceptFriend", "Erreur lors de la suppression de la demande d'ami pour l'utilisateur actuel", task.exception)
            }
        }

        // Après avoir modifié les listes d'amis, rechargez la liste des demandes d'ami pour l'utilisateur actuel
        loadFriendRequests(currentUid)
        loadFriends(currentUid)
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
        loadFriends(currentUid)
    }


}
