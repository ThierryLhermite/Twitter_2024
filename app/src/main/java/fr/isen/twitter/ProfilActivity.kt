package fr.isen.twitter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import fr.isen.twitter.model.TopBar
import java.io.InputStream
import java.util.UUID

class ProfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfilScreen()
            ImageUploadFromGalleryWithSendButton()


        }
    }
}


@Composable
fun ProfilScreen() {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }

    // Récupérer l'UID de l'utilisateur connecté
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Lancer un effet pour récupérer le nom d'utilisateur une seule fois
    LaunchedEffect(uid) {
        uid?.let {
            val databaseReference = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Users").child(it)
            databaseReference.get().addOnSuccessListener { snapshot ->
                username = snapshot.child("username").getValue(String::class.java) ?: "Inconnu"
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                showBackButton = true,
                onNavigateBack = {
                    // Implémentez la logique de navigation vers l'arrière si nécessaire
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Bienvenue, $username", style = MaterialTheme.typography.headlineMedium)
            }
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
        Button(onClick = {
            galleryLauncher.launch("image/*")
        }) {
            Text("Importer une image")
        }

        Spacer(modifier = Modifier.height(16.dp)) // Un peu d'espace entre les boutons

        // Bouton pour envoyer l'image
        Button(onClick = {
            imageUri?.let { uri ->
                uploadImageToFirebaseStorage(uri)
            }
        }, enabled = imageUri != null) { // Désactiver le bouton si aucune image n'est sélectionnée
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
fun uploadImageToFirebaseStorage(imageUri: Uri?) {
    val storageReference = FirebaseStorage.getInstance().reference
    val fileName = UUID.randomUUID().toString() // Crée un nom de fichier unique
    val imageRef = storageReference.child("images/$fileName")

    imageUri?.let { uri ->
        imageRef.putFile(uri)
            .addOnSuccessListener {
                // en cas de succès de l'envoi
                println("Image téléchargée avec succès")
            }
            .addOnFailureListener {
                // en cas d'échec de l'envoi
                println("Échec du téléchargement de l'image")
            }
    }
}
