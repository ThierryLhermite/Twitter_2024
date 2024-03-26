package fr.isen.twitter


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.twitter.model.TopBar
import fr.isen.twitter.ui.theme.MyApplicationTheme

class HomeActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                UserHomeScreen()
            }
        }
    }
}

@Composable
fun UserHomeScreen() {
    //val context = LocalContext.current
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
