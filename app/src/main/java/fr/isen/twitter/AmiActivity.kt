package fr.isen.twitter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import fr.isen.twitter.model.AmiViewModel
import fr.isen.twitter.model.PostViewModel
import fr.isen.twitter.model.TopBar
import fr.isen.twitter.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
        viewModel.loadFriends(uid)
    }

    val friends by viewModel.friends.observeAsState(emptyList())

    Scaffold {
        LazyColumn {
            items(friends) { friendUid ->
                FriendItem(friendUid)
            }
        }
    }
}

@Composable
fun FriendItem(friendUid: String, viewModel: AmiViewModel = viewModel()) {
    var username by remember { mutableStateOf("") }

    LaunchedEffect(friendUid) {
        viewModel.fetchUsername(friendUid) { fetchedUsername ->
            username = fetchedUsername
        }
    }

    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Ami : $username")
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

