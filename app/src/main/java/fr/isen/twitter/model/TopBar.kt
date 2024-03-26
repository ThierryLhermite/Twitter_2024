package fr.isen.twitter.model

import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.isen.twitter.ProfilActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(showBackButton: Boolean, onNavigateBack: () -> Unit) {
    val context = LocalContext.current // Obtenez le contexte local dans le composable
    TopAppBar(
        title = {},
        navigationIcon ={
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Filled.Home, contentDescription = "Accueil",
                        modifier = Modifier.size(35.dp)
                    )
                }
        },
        actions = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var searchText by remember { mutableStateOf("") }
                // Barre de recherche
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    singleLine = true,
                    keyboardActions = KeyboardActions(onSearch = {
                        // Gestion de l'action de recherche
                    }),
                    decorationBox = { innerTextField ->
                        if (searchText.isEmpty()) {
                            Text("Rechercher...", color = Color.Gray)
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                // Bouton de profil
                IconButton(onClick = {
                    // Naviguer vers le profil utilisateur
                    context.startActivity(Intent(context, ProfilActivity::class.java))
                }) {
                    Icon(
                        Icons.Filled.AccountCircle, contentDescription = "Profil",
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        },
    )
}
