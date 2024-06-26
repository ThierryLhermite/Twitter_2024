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
import com.google.firebase.storage.FirebaseStorage
import fr.isen.twitter.Post
import java.text.ParseException
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

    // LiveData pour observer les changements de l'état de like sur un post spécifique
    // Utilisez un dictionnaire pour stocker l'état de like pour chaque post
    private val _postsLikes = MutableLiveData<Map<String, Boolean>>()
    val postsLikes: LiveData<Map<String, Boolean>> = _postsLikes

    // Utilisez un dictionnaire pour stocker le compteur de likes pour chaque post
    private val _postsLikesCount = MutableLiveData<Map<String, Int>>()
    val postsLikesCount: LiveData<Map<String, Int>> = _postsLikesCount

    private val _userLikes = MutableLiveData<Map<String, Boolean>>().apply { value = emptyMap() }
    val userLikes: LiveData<Map<String, Boolean>> = _userLikes


    private val auth = FirebaseAuth.getInstance()
    fun toggleLike(uid: String, postName: String) {
        val currentUid = auth.currentUser?.uid ?: return

        // Path to the specific post's likes
        val postLikesRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$uid/Posts/$postName/Likes")

        postLikesRef.child(currentUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isLiked = snapshot.exists()
                if (isLiked) {
                    postLikesRef.child(currentUid).removeValue()
                } else {
                    postLikesRef.child(currentUid).setValue(true)
                }
                // Met à jour l'état local après le changement dans Firebase
                val updatedLikes = _userLikes.value.orEmpty().toMutableMap().apply {
                    this[postName] = !isLiked
                }
                _userLikes.postValue(updatedLikes)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PostViewModel", "Error toggling like", databaseError.toException())
            }
        })
    }

    fun likeMaj(uid: String, postName: String) {
        val currentUid = auth.currentUser?.uid ?: return

        // Chemin vers les likes du post spécifique
        val postLikesRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$uid/Posts/$postName/Likes/$currentUid")

        postLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Détermine si l'UID de l'utilisateur actuel est présent dans les likes
                val isLiked = snapshot.exists()

                // Met à jour l'état local pour refléter la présence ou l'absence du like
                updateIsLikedState(postName, isLiked)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PostViewModel", "Erreur lors de la récupération de l'état du like", databaseError.toException())
            }
        })
    }

    private fun updateIsLikedState(postName: String, isLiked: Boolean) {
        val updatedLikes = _userLikes.value.orEmpty().toMutableMap().apply {
            this[postName] = isLiked
        }
        _userLikes.postValue(updatedLikes)
    }

    fun fetchLikesCount(uid: String, postName: String) {
        val likesRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("Users/$uid/Posts/$postName/Likes")

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Update the likes count for the specific post
                val currentCounts = _postsLikesCount.value ?: mapOf()
                _postsLikesCount.postValue(currentCounts + (postName to snapshot.childrenCount.toInt()))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostViewModel", "Error fetching likes count", error.toException())
            }
        })
    }
}
