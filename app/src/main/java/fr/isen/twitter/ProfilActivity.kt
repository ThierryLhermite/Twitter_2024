package fr.isen.twitter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import fr.isen.twitter.HomeActivity
import fr.isen.twitter.R
import fr.isen.twitter.model.TopBar
import java.util.UUID

class ProfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("username") ?: "Nom d'utilisateur par défaut"
        setContent {
            ProfilScreen(username)
            ImageUploadFromGalleryWithSendButton()
        }
    }
}


@Composable
fun ProfilScreen(username: String) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopBar(
                onNavigateBack = { context.startActivity(Intent(context, HomeActivity::class.java)) }
            )
        }
    ) { paddingValues ->
        ProfileContent(paddingValues, username)
    }
}

@Composable
fun ProfileContent(paddingValues: PaddingValues, username: String) {
    var friendsCount by remember { mutableStateOf(10) } // Exemple du nombre d'amis
    var isBottomSheetExpanded by remember { mutableStateOf(false) }
    var postDescription by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

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
            Button(
                onClick = { /* Logique du clic ici */ },
            ) {
                Text("$friendsCount amis")
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Ajoute un peu d'espace entre la photo de profil et le bouton "Poster"
        // Bouton pour poster
        Button(
            onClick = { isBottomSheetExpanded = !isBottomSheetExpanded }, // Inverser l'état de l'expansion du menu
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isBottomSheetExpanded) "Fermer" else "Poster")
        }

        if (isBottomSheetExpanded) {
            PostBottomSheet(
                onDismiss = { isBottomSheetExpanded = false },
                onPublish = {
                    uploadPostToFirebase(postDescription, imageUri)
                    isBottomSheetExpanded = false
                },
                postDescription = postDescription,
                onDescriptionChange = { postDescription = it },
                imageUri = imageUri,
                onImageSelected = { imageUri = it }
            )
        }
    }
}


@Composable
fun PostBottomSheet(
    onDismiss: () -> Unit,
    onPublish: () -> Unit,
    postDescription: String,
    onDescriptionChange: (String) -> Unit,
    imageUri: Uri?,
    onImageSelected: (Uri) -> Unit
) {
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
                // Logique pour sélectionner une image ici
                // Vous pouvez lancer une activité pour sélectionner une image à partir de la galerie
                onImageSelected(Uri.EMPTY) // Passer une URI vide pour l'instant
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sélectionner une image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour publier le post
        Button(
            onClick = onPublish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publier")
        }
    }
}

fun uploadImageToFirebaseStorage(imageUri: Uri?) {
    val storageReference = FirebaseStorage.getInstance().reference
    val fileName = UUID.randomUUID().toString() // Crée un nom de fichier unique
    val imageRef = storageReference.child("images/$fileName")

    imageUri?.let { uri ->
        imageRef.putFile(uri)
            .addOnSuccessListener {
                // Traitement en cas de succès de l'envoi
                println("Image téléchargée avec succès")
            }
            .addOnFailureListener {
                // Traitement en cas d'échec de l'envoi
                println("Échec du téléchargement de l'image")
            }
    }
}

@Composable
fun ImageUploadFromGalleryWithSendButton() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column {
        Button(
            onClick = {
                galleryLauncher.launch("image/*")
            }
        ) {
            Text("Sélectionner une image")
        }

        Spacer(modifier = Modifier.height(16.dp)) // Un peu d'espace entre les boutons

        // Bouton pour envoyer l'image
        Button(
            onClick = {
                imageUri?.let { uri ->
                    uploadImageToFirebaseStorage(uri)
                }
            },
            enabled = imageUri != null
        ) { // Désactiver le bouton si aucune image n'est sélectionnée
            Text("Envoyer l'image")
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

fun uploadPostToFirebase(description: String, imageUri: Uri?) {
    // Récupérer l'UID de l'utilisateur
    val userId = Firebase.auth.currentUser?.uid

    // Vérifier si l'utilisateur est authentifié
    if (userId != null) {
        // Logique pour enregistrer le post dans Firebase avec l'UID de l'utilisateur
        val storageReference = FirebaseStorage.getInstance().reference
        val postId = UUID.randomUUID().toString() // Générer un ID unique pour le post
        val postRef = storageReference.child("posts/$postId")

        // Envoyer l'image à Firebase Storage
        imageUri?.let { uri ->
            postRef.putFile(uri)
                .addOnSuccessListener { // Enregistrement réussi dans Firebase Storage
                    // Récupérer l'URL de l'image téléchargée
                    postRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        // Enregistrer les détails du post dans la base de données Firestore
                        val db = Firebase.firestore
                        val post = hashMapOf(
                            "userId" to userId,
                            "description" to description,
                            "imageUrl" to imageUrl
                        )
                        db.collection("posts")
                            .document(postId)
                            .set(post)
                            .addOnSuccessListener {
                                // Succès de l'enregistrement du post dans Firestore
                                println("Post enregistré avec succès dans Firestore")
                            }
                            .addOnFailureListener { e ->
                                // Échec de l'enregistrement du post dans Firestore
                                println("Erreur lors de l'enregistrement du post dans Firestore: $e")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Échec de l'envoi de l'image à Firebase Storage
                    println("Erreur lors de l'envoi de l'image à Firebase Storage: $e")
                }
        }
    } else {
        println("L'utilisateur n'est pas authentifié.")
    }
}