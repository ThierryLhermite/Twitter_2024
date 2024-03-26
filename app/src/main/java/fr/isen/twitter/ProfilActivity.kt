package fr.isen.twitter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.isen.twitter.R

class ProfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfilScreen()
        }
    }
}


@Composable
fun ProfilScreen() {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopBar(
                onNavigateBack = { context.startActivity(Intent(context, HomeActivity::class.java)) }
            )
        }
    ) { paddingValues ->
        ProfileContent(paddingValues)
    }
}

@Composable
fun ProfileContent(paddingValues: PaddingValues) {
    var username by remember { mutableStateOf("Nom d'utilisateur") }
    var friendsCount by remember { mutableStateOf(10) } // Exemple du nombre d'amis

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
                Text("Amis: $friendsCount")
            }
        }
        // Ajoutez ici d'autres composants ou informations de profil si nécessaire
    }
}