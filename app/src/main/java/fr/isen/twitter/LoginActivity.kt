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
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.twitter.model.AuthViewModel
import androidx.compose.runtime.livedata.observeAsState

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
        )
        Button(onClick = {
            authViewModel.login(email, password)
        }) {
            Text("Login")
        }
        Button(onClick = {
            context.startActivity(Intent(context, RegisterActivity::class.java))
        }) {
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


