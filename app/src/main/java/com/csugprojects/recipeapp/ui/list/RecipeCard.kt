package com.csugprojects.recipeapp.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.csugprojects.recipeapp.domain.model.Recipe
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * RecipeCard is the custom, reusable component for displaying a single recipe summary (View Layer - M2 Component).
 * It is used by the list and home screens to display scrollable content.
 */
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // AsyncImage uses the Coil library to load images from the network (M4 Implementation).
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = "Image of ${recipe.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // IconButton manages the favorite status logic (M2 Feature).
                IconButton(
                    onClick = { onFavoriteClick(!recipe.isFavorite) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    // Icon image changes based on the recipe's current favorite status (M6 State Management).
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        // Content description ensures accessibility (M2 Accessibility).
                        contentDescription = "Favorite",
                        tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}