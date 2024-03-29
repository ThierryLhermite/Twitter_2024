package fr.isen.twitter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import fr.isen.twitter.model.PostViewModel
import fr.isen.twitter.model.TopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("username") ?: "Nom d'utilisateur par défaut"
        setContent {
            ProfilScreen(username)
            ChangeProfilePicture()
        }
    }
}


@Composable
fun ProfilScreen(username: String, PostViewModel: PostViewModel = viewModel()) {
    val context = LocalContext.current
    LaunchedEffect(username) {
        PostViewModel.fetchUidByUsername(username)
    }
    val uid  by PostViewModel.userUid.observeAsState("")

    // Log l'UID chaque fois qu'il change
    LaunchedEffect(uid) {
        Log.d("ProfilScreen", "UID récupéré: $uid")
    }

    Scaffold(
        topBar = {
            TopBar(
                onNavigateBack = { context.startActivity(Intent(context, HomeActivity::class.java)) }
            )
        }
    ) { paddingValues ->
        Column {

            uid?.let {
                ProfileContent(paddingValues, username, uid = it)
                PostsScreen(uid = it, viewModel = viewModel())
            }
        }
    }
}

@Composable
fun ProfileContent(paddingValues: PaddingValues, username: String, uid : String) {
    var friendsCount by remember { mutableStateOf(10) } // Exemple du nombre d'amis
    var isBottomSheetExpanded by remember { mutableStateOf(false) }
    var postDescription by remember { mutableStateOf("") }
    val user = FirebaseAuth.getInstance().currentUser
    val currentuid = user?.uid
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Utilisez Row pour aligner horizontalement la photo de profil, le nom d'utilisateur et le bouton "Amis"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile_placeholder),
                contentDescription = "Photo de profil",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f) // Assure que le texte prend l'espace disponible
            )
            Column {
                Button(
                    onClick = {
                            val intent = Intent(context, AmiActivity::class.java).apply {
                                putExtra("uid", uid)
                            }
                        context.startActivity(intent)

                    },
                ) {
                    Text("$friendsCount amis")
                }
                // Bouton de déconnexion
                if (currentuid==uid){

                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            // Rediriger vers l'écran de connexion ou la page d'accueil
                            context.startActivity(Intent(context, LoginActivity::class.java)) // Assurez-vous d'avoir une activité de connexion nommée LoginActivity ou changez selon votre implémentation
                        },
                    ) {
                        Text("Déconnexion")
                    }
                }
                else{
                    Button(
                        onClick = {
                            addFriend(uid)
                        },
                    ) {
                        Text("Demande d'ami")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Ajoute un peu d'espace entre la photo de profil et le bouton "Poster"
        // Bouton pour poster
        if (currentuid==uid){
            Button(
                onClick = { isBottomSheetExpanded = !isBottomSheetExpanded }, // Inverser l'état de l'expansion du menu
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isBottomSheetExpanded) "Fermer" else "Poster")
            }
            if (isBottomSheetExpanded) {
                PostBottomSheet(
                    postDescription = postDescription,
                    onDescriptionChange = { postDescription = it }
                )
            }
        }
    }
}

fun addFriend(targetUid: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid ?: return

    // Référence à la liste des demandes d'ami de l'UID cible
    val friendRequestsRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("Users/$targetUid/friendRequests/$currentUid") // Utilise currentUid comme clé directement

    // Vérifie d'abord si une demande d'ami existe déjà de cet utilisateur
    friendRequestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!snapshot.exists()) {
                // Si aucune demande d'ami n'existe de cet utilisateur, ajoutez-la
                friendRequestsRef.setValue(true).addOnCompleteListener { task -> // Attribue true comme valeur
                    if (task.isSuccessful) {
                        Log.d("AddFriend", "Demande d'ami envoyée avec succès.")
                    } else {
                        Log.e("AddFriend", "Erreur lors de l'envoi de la demande d'ami", task.exception)
                    }
                }
            } else {
                Log.d("AddFriend", "Une demande d'ami existe déjà pour cet utilisateur.")
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("AddFriend", "Erreur lors de la vérification de l'existence de la demande d'ami", databaseError.toException())
        }
    })
}



@Composable
fun PostBottomSheet(
    postDescription: String,
    onDescriptionChange: (String) -> Unit,
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    Column {
        // Input pour la description du post
        TextField(
            value = postDescription,
            onValueChange = { onDescriptionChange(it) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour sélectionner une image
        Button(
            onClick = {
                galleryLauncher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sélectionner une image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour publier le post
        Button(onClick = {
            imageUri?.let { uri ->
                UploadPost(uri, description = "$postDescription")
            }
        }, enabled = imageUri != null) { // Désactiver le bouton si aucune image n'est sélectionnée
            Text("Publier")
        }
        imageUri?.let { uri ->
            // Utiliser Coil pour charger et afficher l'image
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Image sélectionnée"
            )
        }
    }
}

fun UploadPost(imageUri: Uri?,description: String) {
    val storageReference = FirebaseStorage.getInstance().reference
    val postname =UUID.randomUUID().toString()
    val fileName = UUID.randomUUID().toString() // Crée un nom de fichier unique
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val imageRef =storageReference.child("/$fileName")

    imageUri?.let { uri ->
        imageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                // L'image a été téléchargée, on obtien son URL
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    // Obtiens la date actuelle
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )

                    // Prépare les données à se sauvegarder dans Firestore
                    val postData = mapOf(
                        "image" to imageUrl,
                        "date" to currentDate,
                        "description" to description
                    )



                    // Spécifie le chemin où sauvegarder les données dans la base de données
                    val databaseReference = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
                        .getReference("Users/$uid/Posts/$postname")

                    databaseReference.setValue(postData).addOnCompleteListener { dbTask ->
                        if (!dbTask.isSuccessful) {
                            Log.e("AuthViewModel", "Erreur de sauvegarde des données utilisateur", dbTask.exception)
                        }
                    }

                }

                // Traitement en cas de succès de l'envoi
                println("Image téléchargée avec succès")
            }
            .addOnFailureListener {
                // Traitement en cas d'échec de l'envoi
                println("Échec du téléchargement de l'image")
            }
    }
}

fun UploadComment(uid: String,commentaire: String, postname : String) {
    val auth = FirebaseAuth.getInstance()
    val currentuid = auth.currentUser?.uid
    val Commentname = UUID.randomUUID().toString() // Crée un nom de fichier unique


    // Obtiens la date actuelle
    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
        Date()
    )

    // Prépare les données à se sauvegarder dans Firestore
    val postComment = mapOf(
        "Texte" to commentaire,
        "date" to currentDate,
        "auteur" to currentuid
    )

    // Spécifie le chemin où sauvegarder les données dans la base de données
    val databaseReference = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("Users/$uid/Posts/$postname/Comment/$Commentname")
    databaseReference.setValue(postComment).addOnCompleteListener { dbTask ->
        if (!dbTask.isSuccessful) {
            Log.e("AuthViewModel", "Erreur de sauvegarde des données utilisateur", dbTask.exception)
        }
    }
}

fun upload(uid: String, postName: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid

    if (currentUid == null) {
        Log.e("toggleLike", "Utilisateur non connecté.")
        return
    }

    val likesRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("Users/$uid/Posts/$postName/Likes")

    // Vérifie si l'UID est déjà présent dans la liste des likes
    likesRef.child(currentUid).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Si l'UID est déjà là, le retirer (dislike)
            if (snapshot.exists()) {
                likesRef.child(currentUid).removeValue().addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("toggleLike", "Erreur lors de la suppression du like", task.exception)
                    } else {
                        Log.d("toggleLike", "Like retiré avec succès.")
                    }
                }
            } else {
                // Si l'UID n'est pas là, l'ajouter (like)
                likesRef.child(currentUid).setValue(true).addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("toggleLike", "Erreur lors de l'ajout du like", task.exception)
                    } else {
                        Log.d("toggleLike", "Like ajouté avec succès.")
                    }
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("toggleLike", "Erreur lors de la vérification du like", databaseError.toException())
        }
    })
}

data class Commentaire(
    var uidcomment : String? = null,
    var texte: String? = null,
    var uid: String? = null,
    var date: String? = null
)

data class Post(
    var uidpost : String? = null,
    var uid: String? = null, // Ajout de l'UID de l'utilisateur
    val image: String? = null,
    val date: String? = null, // Assurez-vous que ceci est dans un format triable
    val description: String? = null,
    var commentaires: List<Commentaire>? = mutableListOf(), // Liste des commentaires
    var likes: MutableList<String> = mutableListOf() // Ajout de la liste des UID des utilisateurs qui ont aimé le post
)


@Composable
fun DisplayPosts(posts: List<Post>) {
    LazyColumn { // Utilisez LazyColumn pour afficher une liste d'éléments
        items(posts) { post ->
            PostItem(post = post)
        }
    }
}

@Composable
fun PostItem(post: Post, PostViewModel: PostViewModel = viewModel()) {
    // Variable d'état pour contrôler l'affichage de l'entrée de texte
    var showInput by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(false) }
    val postsLikes by PostViewModel.postsLikes.observeAsState(initial = mapOf())
    val postsLikesCount by PostViewModel.postsLikesCount.observeAsState(initial = mapOf())
    val userLikes by PostViewModel.userLikes.observeAsState(initial = emptyMap())

    // Determine if the current post is liked
    val isLiked = userLikes[post.uidpost] ?: false
    // Get the current like count for the post
    val likeCount = postsLikesCount[post.uidpost] ?: 0

    // Effet lancé pour charger le nombre de likes une fois et s'abonner aux mises à jour
    LaunchedEffect(post.uidpost) {
        post.uid?.let { uid ->
            post.uidpost?.let { uidPost ->
                PostViewModel.likeMaj(uid, uidPost)
                PostViewModel.fetchLikesCount(uid, uidPost)
            }
        }
    }
    LaunchedEffect(post.uid) {
        post.uid?.let { PostViewModel.fetchUsernameByUid(it)
        }
    }

    // Observe le username récupéré
    val username by PostViewModel.username.observeAsState("Username inconnu")


    val uid= post.uid
    val postname = post.uidpost

    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color(0xFFF0F0F0)) // Applique un fond gris léger
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = username) // Utilise le username observé ici
            Text(text = post.date ?: "Date inconnue")
        }
        post.image?.let { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(post.description ?: "")
            IconButton(onClick = {
                post.uid?.let { uid ->
                    post.uidpost?.let { postUid ->
                        PostViewModel.toggleLike(uid, postUid)
                        PostViewModel.fetchLikesCount(uid, postUid)
                    }
                }
            }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }
            Text(text = "$likeCount")
        }
        Button(onClick = { showComments = !showComments }) {
            Text("Commentaires")
        }
        if (showComments) {
            post.commentaires?.let { commentaires ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp) // Limite la hauteur de LazyColumn
                ) {
                    items(commentaires) { commentaire ->
                        CommentItem(commentaire)
                    }
                }
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ajouter un commentaire...") }
                )
                Button(
                    onClick = {
                        if (uid != null) {
                            if (postname != null) {
                                UploadComment(uid,inputText , postname ?: "")
                            }
                        }
                        // Logique d'envoi du commentaire
                        Log.d("PostItem", "Commentaire envoyé: $inputText")
                        // Réinitialiser l'entrée de texte et cacher le champ
                        inputText = ""
                        showInput = false
                    }
                ) {
                    Text("Envoyer")
                }
            }
        }
    }
}

@Composable
fun CommentItem(commentaire: Commentaire,PostViewModel: PostViewModel = viewModel()) {
    // Récupère le username dès que PostItem est appelé avec un UID spécifique
    LaunchedEffect(commentaire.uid) {
        commentaire.uid?.let { PostViewModel.fetchUsernameByUid(it) }
    }

    // Observe le username récupéré
    val username by PostViewModel.username.observeAsState("Username inconnu")


    Column(modifier = Modifier.padding(4.dp)) {
        Log.d("CommentItem", "UID: ${commentaire.uid}, Date: ${commentaire.date}, Texte: ${commentaire.texte}")
        Text(text = "Par $username le ${commentaire.date}")
        Text(text = commentaire.texte ?: "")
    }
}


@Composable
fun PostsScreen(uid: String, viewModel: PostViewModel = viewModel()) {
    // Lorsque PostsScreen est composé, on appelle downloadPosts avec l'UID donné
    LaunchedEffect(uid) {
        viewModel.downloadPosts(uid)
    }

    val posts by viewModel.posts.observeAsState(emptyList())
    Log.d("PostViewModel", "Téléchargement des posts pour l'UID: $uid")

    DisplayPosts(posts)
}


@Composable

fun ChangeProfilePicture() {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val storageReference = FirebaseStorage.getInstance().reference
    val databaseReference = Firebase.database("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app").reference

    var imageUrl by remember { mutableStateOf("") }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> val imageRef = storageReference.child("profilePictures/${uid}.jpg")
                val uploadTask = imageRef.putFile(uri)
                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        imageUrl = uri.toString()
                        // Mise à jour de la Realtime Database avec l'URL de l'image
                        databaseReference.child("Users").child("$uid").child("profilePictureUrl").setValue(imageUrl)
                    }
                }
            }
        }
    }

    Column {
        Button(onClick = {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            launcher.launch(intent)
        }) {
            Text("Changer photo de profil")
        }
        if (imageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Photo de profil",
                modifier = Modifier
                    .padding(
                        horizontal = 15.dp,
                        vertical = 48.dp
                    )
                    .size(100.dp) // Taille de l'icône
                    .clip(CircleShape) // Forme circulaire pour l'icône
            )
        }
    }
}