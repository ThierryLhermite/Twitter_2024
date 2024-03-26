package fr.isen.twitter


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.isen.twitter.ui.theme.MyApplicationTheme
import kotlin.random.Random

data class Comment(
    val userName: String,
    val text: String
)

data class MockPost(
    val postId: String,
    val userImage: Int,
    val userName: String,
    val image: Int,
    val description: String,
    var likes: Int,
    var hasLiked: Boolean, // Champ pour vérifier si l'utilisateur a déjà liké
    var comments: MutableList<Comment>
)

val currentUser = "CurrentUser" // Nom de l'utilisateur actuel

val mockPosts = MutableList(100) {
    MockPost(
        postId = java.util.UUID.randomUUID().toString(),
        userImage = R.drawable.ic_launcher_background, // Remplacez par votre drawable
        userName = "User ${Random.nextInt(1, 100)}",
        image = R.drawable.ic_launcher_background, // Mettez votre image de post ici
        description = "This is a random description ${Random.nextFloat()}",
        likes = 0,
        hasLiked = false,
        comments = mutableListOf()
    )
}

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                SocialFeedScreen(mockPosts)
            }
        }
    }
}

@Composable
fun SocialFeedScreen(posts: List<MockPost>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        FeedScreen(posts)
    }
}

@Composable
fun FeedScreen(posts: List<MockPost>) {
    LazyColumn {
        items(posts) { post ->
            PostItem(post = post, userName = currentUser)
        }
    }
}

@Composable
fun PostItem(post: MockPost, userName: String) {
    var likes by remember { mutableStateOf(post.likes) }
    var hasLiked by remember { mutableStateOf(post.hasLiked) }
    var commentText by remember { mutableStateOf("") }
    var isCommentExpanded by remember { mutableStateOf(false) } // Ajout d'un état pour gérer l'expansion des commentaires

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = post.userImage),
                    contentDescription = "User Image",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
                Text(text = post.userName, modifier = Modifier.padding(8.dp))
            }
            Image(
                painter = painterResource(id = post.image),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentScale = ContentScale.FillWidth
            )
            Text(text = post.description, modifier = Modifier.padding(8.dp))

            // Intégration des boutons Like et Post avec les suggestions de modifications
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (hasLiked) {
                            likes--
                            hasLiked = false
                        } else {
                            likes++
                            hasLiked = true
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = if (hasLiked) painterResource(id = R.drawable.a) else painterResource(id = R.drawable.a),
                        contentDescription = "Like Button"
                    )
                }
                Text(text = "$likes likes", modifier = Modifier.padding(start = 8.dp))
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { isCommentExpanded = !isCommentExpanded },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (isCommentExpanded) R.drawable.b else R.drawable.c),
                        contentDescription = "Toggle Comment Button"
                    )
                }
            }

            // Affichage des commentaires
            if (isCommentExpanded) {
                Column(modifier = Modifier.padding(8.dp)) {
                    post.comments.forEach { comment ->
                        Text(text = "${comment.userName}: ${comment.text}", modifier = Modifier.padding(bottom = 4.dp))
                    }
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Add a comment") },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                post.comments.add(Comment(userName = userName, text = commentText))
                                commentText = ""
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        enabled = commentText.isNotBlank()
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        SocialFeedScreen(mockPosts)
    }
}


