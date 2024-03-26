package fr.isen.twitter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import fr.isen.twitter.ui.theme.MyApplicationTheme
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val database = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
                    val databaseReference = database.reference
                    databaseReference.child("test").setValue("Hello Firebase")
                    databaseReference.child("test").get().addOnSuccessListener { snapshot ->
                        Log.d("Firebase", "Valeur récupérée : ${snapshot.value}")
                    }.addOnFailureListener {
                        Log.e("Firebase", "Erreur de lecture", it)
                    }
                    Greeting(name = "SchoolShare")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoss),
            contentDescription = "Image d'exemple",
            modifier = Modifier.size(200.dp) // Définissez une taille deux fois plus grande (200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp)) // Ajoutez un espace de 16dp entre l'image et le texte
        Text(
            text = "$name",
            fontSize = 24.sp // Définissez une taille de police deux fois plus grande (24.sp)
        )
        Spacer(modifier = Modifier.height(16.dp)) // Ajoutez un espace de 16dp entre le texte et le bouton
        Button(
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        ) {
            Text("Aller à la page de connexion", fontSize = 16.sp) // Définissez une taille de police deux fois plus grande (16.sp) pour le texte du bouton
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}