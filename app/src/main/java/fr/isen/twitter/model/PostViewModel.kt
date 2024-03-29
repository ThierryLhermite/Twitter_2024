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
import com.google.firebase.storage.FirebaseStorage
import fr.isen.twitter.Post
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class PostViewModel : ViewModel() {



    private val _username = MutableLiveData<String>()
    private val _profilePictureUrl = MutableLiveData<String>()
    val username: LiveData<String> = _username
    val profilePictureUrl: LiveData<String> = _profilePictureUrl

    fun fetchUsernameByUid(uid: String) {
        val userRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users/$uid/username")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Récupère le username et le stocke dans le LiveData
                _username.value = snapshot.value as? String
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("UserViewModel", "fetchUsernameByUid:onCancelled", error.toException())
                // Gérer l'erreur si nécessaire
            }
        })
    }
    fun fetchPhotosByUid(uid: String) {
        val userRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users/$uid/profilePictureUrl")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Récupère le lien URL de la photo de profil username et le stocke dans le LiveData
                _profilePictureUrl.value = snapshot.value as? String
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("URL", "Valeur URL Image: $String")

                // Gérer l'erreur si nécessaire
            }
        })
    }

    private val _userUid = MutableLiveData<String?>()
    val userUid: MutableLiveData<String?> = _userUid

    fun fetchUidByUsername(usernameToFind: String) {
        val usersRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { userSnapshot ->
                    val username = userSnapshot.child("username").value as? String
                    if (username == usernameToFind) {
                        val uid = userSnapshot.key
                        _userUid.postValue(uid) // Supposons que _userUid est un MutableLiveData<String>
                        return
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ViewModel", "Failed to fetch UID.", databaseError.toException())
            }
        })
    }
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    fun downloadPosts(uid: String) {
        val databaseReference = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$uid/Posts")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postsList = mutableListOf<Post>()
                snapshot.children.forEach { postSnapshot ->
                    val uidpost = postSnapshot.key
                    val post = postSnapshot.getValue(Post::class.java)?.apply {
                        this.uid = uid // Assurez-vous d'ajouter l'UID ici
                        this.uidpost = uidpost // Ajoutez l'UID du post ici
                    }
                    post?.let { postsList.add(it) }
                }
                // Triez ici si nécessaire
                val sortedPosts = postsList.sortedByDescending { it.date?.let { date -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date)?.time } ?: 0L }
                _posts.value = sortedPosts
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PostViewModel", "Échec du chargement des posts.", databaseError.toException())
            }
        })
    }


}
