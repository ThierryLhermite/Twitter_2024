package fr.isen.twitter


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import fr.isen.twitter.model.TopBar

class SearchResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val searchText = intent.getStringExtra("searchText") ?: ""
        setContent {
            // Votre UI pour afficher les résultats, par exemple :
            SearchResultsScreen(searchText)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(searchText: String) {
    val searchResults = remember { mutableStateListOf<String>() }
    val context = LocalContext.current
    LaunchedEffect(searchText) {
        if (searchText.isNotBlank()) {
            val usersRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
                .reference.child("Users")
            usersRef.get().addOnSuccessListener { dataSnapshot ->
                searchResults.clear()
                for (userSnapshot in dataSnapshot.children) {
                    // Supposons que chaque utilisateur a un champ 'username' sous leur UID
                    val username = userSnapshot.child("username").getValue<String>()
                    if (username != null && username.startsWith(searchText, ignoreCase = true)) {
                        searchResults.add(username)
                    }
                }
            }.addOnFailureListener {
                // Gérer l'erreur
            }
        } else {
            searchResults.clear()
        }
    }
    Scaffold(
        topBar = {
                TopBar(
                    onNavigateBack = { context.startActivity(Intent(context, HomeActivity::class.java)) }
                ) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(searchResults) { result ->
                Text(
                    text = result,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}