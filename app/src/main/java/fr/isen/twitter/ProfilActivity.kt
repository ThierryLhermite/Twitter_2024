package fr.isen.twitter

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.twitter.model.TopBar

class ProfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfilScreen()
            ImageUpload()
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

fun ImageUpload() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Préparer le launcher pour ouvrir la galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri // Mettre à jour l'URI de l'image sélectionnée
    }

    Column {
        Button(onClick = {
            // Demander à l'utilisateur de sélectionner une image
            galleryLauncher.launch("image/*")
        }) {
            Text("Importer une image")
        }

        // Afficher l'image sélectionnée
        imageUri?.let {
            // Utiliser un composant pour afficher l'image, par exemple AsyncImage de Coil
            Text("Image sélectionnée : $it")
        }
    }
}