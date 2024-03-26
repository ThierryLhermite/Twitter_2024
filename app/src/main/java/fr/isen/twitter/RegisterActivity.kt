package fr.isen.twitter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.twitter.model.AuthViewModel

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen()
            // Votre thème Compose (si applicable)

        }
    }
}

@Composable
fun RegisterScreen(authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("username") },
        )
        Button(onClick = {
            authViewModel.register(email, password, username)
        })
        {
            Text("Register")
        }

    }
    val user by authViewModel.userLiveData.observeAsState()

    LaunchedEffect(user) {
        if (user != null) {
            // Utilisateur connecté, naviguez vers une autre activité
            context.startActivity(Intent(context, HomeActivity::class.java))
            // Vous pourriez vouloir finir LoginActivity ici, mais soyez prudent car vous êtes dans un contexte Composable.
            // Il pourrait être préférable de gérer la navigation en dehors du Composable, ou d'utiliser un système de navigation adapté à Compose.
        }
    }
}


