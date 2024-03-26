package fr.isen.twitter.model

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import fr.isen.twitter.ProfilActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val searchText = remember { mutableStateOf("") }

    // Utiliser Box pour superposer les éléments
    Box {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Filled.Home, contentDescription = "Accueil",
                        modifier = Modifier.size(35.dp)
                    )
                }
            },
            actions = {
                Spacer(Modifier.weight(1f, fill = true)) // Cela pousse les actions à l'extrémité opposée
                IconButton(onClick = { context.startActivity(Intent(context, ProfilActivity::class.java)) }) {
                    Icon(
                        Icons.Filled.AccountCircle, contentDescription = "Profil",
                        modifier = Modifier.size(35.dp)
                    )
                }
            },
        )

        // Positionnement de la barre de recherche au centre
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SearchBar()
        }
    }
}


@Composable
fun SearchBar() {
    val searchTextState = remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<String>() }
    val context = LocalContext.current

    // Mise à jour des résultats de recherche en fonction du texte entré
    LaunchedEffect(searchTextState.value) {
        val searchText = searchTextState.value
        if (searchText.isNotBlank()) {
            val usersRef = FirebaseDatabase.getInstance("https://twitter-42a5c-default-rtdb.europe-west1.firebasedatabase.app")
                .reference.child("Users")
            usersRef.get().addOnSuccessListener { dataSnapshot ->
                searchResults.clear()
                dataSnapshot.children.forEach { userSnapshot ->
                    val username = userSnapshot.child("username").getValue<String>()
                    if (username != null && username.startsWith(searchText, ignoreCase = true)) {
                        searchResults.add(username)
                    }
                }
            }
        } else {
            searchResults.clear()
        }
    }

    Column {
        OutlinedTextField(
            value = searchTextState.value,
            onValueChange = { newText ->
                searchTextState.value = newText
            },
            placeholder = { Text("Rechercher...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Recherche") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                // Vous pourriez vouloir traiter la recherche ici ou fermer le clavier
            })
        )

        // Affichage des suggestions de recherche
        LazyColumn {
            items(searchResults) { result ->
                Text(
                    text = result,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clickable {
                            // Lancer ProfilActivity avec le nom d'utilisateur sélectionné
                            val intent = Intent(context, ProfilActivity::class.java).apply {
                                putExtra(
                                    "username",
                                    result
                                ) // Assurez-vous que "username" correspond à la clé attendue dans ProfilActivity
                            }
                            context.startActivity(intent)
                        }
                )
            }
        }
    }
}
