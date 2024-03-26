package fr.isen.twitter


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.twitter.model.AuthViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image


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
    var passwordVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image de votre logo
        Image(
            painter = painterResource(id = R.drawable.logoss),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )

        // Champ d'e-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textStyle = TextStyle(fontSize = 16.sp)
        )

        // Champ de mot de passe avec option de commutation
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textStyle = TextStyle(fontSize = 16.sp)
            )

            // Option de commutation pour afficher ou masquer le mot de passe
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    painter = if (passwordVisible) painterResource(id = R.drawable.ouvert) else painterResource(id = R.drawable.ferme),
                    contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                )
            }
        }

        // Bouton de connexion et bouton d'inscription
        Row(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(
                onClick = {
                    authViewModel.login(email, password)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Login", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Register", fontSize = 18.sp)
            }
        }
    }

    // État de l'utilisateur observé
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
