package fr.isen.twitter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
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
                postDescription = postDescription,
                onDescriptionChange = { postDescription = it }
            )
        }
    }
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
                UploadPost(uri, description = "c moi")
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
                // L'image a été téléchargée avec succès, maintenant obtenez son URL
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    // Obtenez la date actuelle
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )

                    // Préparez les données à sauvegarder dans Firestore
                    val postData = mapOf(
                        "image" to imageUrl,
                        "date" to currentDate,
                        "description" to description
                    )



                    // Spécifiez le chemin où vous voulez sauvegarder les données dans votre base de données
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