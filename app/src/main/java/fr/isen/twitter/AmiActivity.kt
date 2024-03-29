package fr.isen.twitter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import fr.isen.twitter.model.AmiViewModel
import fr.isen.twitter.model.TopBar
import fr.isen.twitter.ui.theme.MyApplicationTheme

class AmiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = intent.getStringExtra("uid") ?: return // Récupère l'UID passé à l'activité
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid ?: return

        setContent {
            MyApplicationTheme {
                Scaffold(
                    topBar = {
                        TopBar(
                            onNavigateBack = {
                                val homeIntent = Intent(this, HomeActivity::class.java)
                                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                this.startActivity(homeIntent)
                            }
                        )
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (currentUid == uid) {
                                // Ici, vous pouvez utiliser l'UID pour charger les demandes d'ami
                                FriendRequestsScreen(uid)
                            }
                            FriendScreen(uid)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FriendRequestsScreen(uid: String) {
    // Utilisez un ViewModel pour charger les demandes d'ami
    val viewModel: AmiViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(uid) {
        viewModel.loadFriendRequests(uid)
    }

    val friendRequests by viewModel.friendRequests.observeAsState(emptyList())

    Scaffold {
        // Remplacez ce LazyColumn par votre propre UI pour afficher les demandes d'ami
        LazyColumn {
            items(friendRequests) { requestUid ->
                FriendRequestItem(requestUid)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FriendScreen(uid: String) {
    val viewModel: AmiViewModel = viewModel()
    LaunchedEffect(uid) {
        Log.d("FriendScreen", "LaunchedEffect avec UID: $uid")
        viewModel.loadFriends(uid)
    }

    val friends by viewModel.friends.observeAsState(emptyList())

    Scaffold {
        LazyColumn {
            items(friends) { friendUid ->
                FriendItem(friendUid, viewModel= viewModel,uid)
            }
        }
    }
}

@Composable
fun FriendItem(friendUid: String, viewModel: AmiViewModel,uid: String) {
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(friendUid) {
        Log.d("FriendItem", "Récupération du nom pour l'UID: $friendUid")
        viewModel.fetchUsername(friendUid) { fetchedUsername ->
            Log.d("FriendItem", "Nom récupéré: $fetchedUsername")
            username = fetchedUsername
        }
    }
    LaunchedEffect(uid) {
        Log.d("FriendScreen", "LaunchedEffect avec UID: $uid")
        viewModel.loadFriends(uid)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = username)

        Button(
            onClick = {
                // Implémentez la logique pour supprimer cet ami ici
                viewModel.removeFriend(friendUid) {
                    Toast.makeText(context, "Ami supprimé : $username", Toast.LENGTH_SHORT).show()
                }
            },
            // Appliquer un style ou des modifications de modifier au besoin
        ) {
            Text("Supprimer")
        }
    }
}




@Composable
fun FriendRequestItem(requestUid: String, viewModel: AmiViewModel = viewModel() ) {
    var username by remember { mutableStateOf("") } // Initialisez l'état pour stocker le username

    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid ?: return

    LaunchedEffect(requestUid) {
        viewModel.fetchUsername(requestUid) { fetchedUsername ->
            username = fetchedUsername
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),

    ) {
        Text(text = "Demande d'ami de : $username")

        // Groupe de boutons pour accepter ou refuser la demande
        Row {
            Button(
                onClick = {
                    viewModel.acceptFriend(currentUid, requestUid)
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Accepter")
            }

            Button(
                onClick = {
                    viewModel.refuseFriend(currentUid, requestUid)
                },
            ) {
                Text("Refuser")
            }
        }
    }
}

