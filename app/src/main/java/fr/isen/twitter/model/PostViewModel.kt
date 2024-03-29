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
import fr.isen.twitter.Commentaire
import fr.isen.twitter.Post
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class PostViewModel : ViewModel() {


    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

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
                    // Identifiant unique du post
                    val uidpost = postSnapshot.key
                    // Récupère et construit la liste des commentaires pour ce post spécifique
                    // Récupère et construit la liste des commentaires pour ce post spécifique
                    val commentaires = mutableListOf<Commentaire>()
                    postSnapshot.child("Comment").children.forEach { commentaireSnapshot ->
                        val uidCommentaire = commentaireSnapshot.key ?: ""
                        val auteurCommentaire = commentaireSnapshot.child("auteur").getValue(String::class.java) ?: ""
                        val texteCommentaire = commentaireSnapshot.child("Texte").getValue(String::class.java) ?: ""
                        val dateCommentaire = commentaireSnapshot.child("date").getValue(String::class.java) ?: ""

                        val commentaire = Commentaire().apply {
                            this.uidcomment = uidCommentaire // Définit l'UID du commentaire
                            this.uid = auteurCommentaire
                            this.texte = texteCommentaire // Définit le texte du commentaire
                            this.date = dateCommentaire // Définit la date du commentaire
                        }

                        commentaires.add(commentaire)
                    }

                    // Trie les commentaires par date dans l'ordre décroissant avant de les ajouter au post
                    val sortedCommentaires = commentaires.sortedByDescending {
                        try {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.date)?.time ?: 0L
                        } catch (e: ParseException) {
                            // Log the error or handle it as needed
                            Log.e("PostViewModel", "Failed to parse date: '${it.date}'.", e)
                            0L // Provide a default value in case of parsing failure
                        }
                    }

                    val post = postSnapshot.getValue(Post::class.java)?.apply {
                        this.uid = uid // Définit l'UID de l'utilisateur qui a créé le post
                        this.uidpost = uidpost // Définit l'UID unique du post
                        this.commentaires = sortedCommentaires // Ajoute la liste triée des commentaires à ce post
                    }
                    post?.let { postsList.add(it) }
                }
                // Trie les posts par date dans l'ordre décroissant
                val sortedPosts = postsList.sortedByDescending {
                    it.date?.let { date ->
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date)?.time
                    } ?: 0L
                }
                // Met à jour l'état ou la variable observée qui contient la liste des posts
                _posts.value = sortedPosts
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PostViewModel", "Échec du chargement des posts.", databaseError.toException())
            }
        })
    }

}
