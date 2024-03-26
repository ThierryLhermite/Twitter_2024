package fr.isen.twitter

import android.content.Intent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.isen.twitter.model.TopBar
import fr.isen.twitter.ui.theme.MyApplicationTheme
import kotlin.random.Random

data class MockPost(
    val postId: String,
    val userImage: Int,
    val userName: String,
    val image: Int,
    val description: String,
    val likes: Int,
    val comments: Int
)

val mockPosts = List(100) {
    val uid = java.util.UUID.randomUUID().toString()
    MockPost(uid, R.drawable.ic_launcher_background, "User ${Random.nextInt(1, 100)}",
        R.drawable.ic_launcher_background, "This is a random description ${Random.nextFloat()}",
        Random.nextInt(1000), Random.nextInt(100))
}

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Scaffold(
                    topBar = {
                        TopBar(
                            showBackButton = true,
                            onNavigateBack = {
                                val homeIntent = Intent(this, HomeActivity::class.java)
                                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                this.startActivity(homeIntent)
                            }
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SocialFeedScreen()
                        }
                    }
                }
                SocialFeedScreen(mockPosts)
            }
        }
    }
}

@Composable
fun SocialFeedScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        FeedScreen()
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
fun StoryCard(post: MockPost) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .size(70.dp)
            .clip(CircleShape),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Image(
            painter = painterResource(id = post.userImage),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun PostItem(post: MockPost) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = post.userImage),
                    contentDescription = null,
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
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentScale = ContentScale.FillWidth
            )
            Text(text = post.description, modifier = Modifier.padding(8.dp))
            Text(text = "${post.likes} likes", modifier = Modifier.padding(8.dp))
            Text(text = "${post.comments} comments", modifier = Modifier.padding(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        SocialFeedScreen()
    }
}
