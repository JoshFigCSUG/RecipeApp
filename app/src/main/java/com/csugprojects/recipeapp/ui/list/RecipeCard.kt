package com.csugprojects.recipeapp.ui.list

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.csugprojects.recipeapp.domain.model.Recipe
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * RecipeCard is the custom, reusable component for displaying a single recipe summary (View Layer - M2 Component).
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
            // Enables the entire card to be clicked to view details (M2 Navigation Flow).
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        // Increased elevation for a prominent card style (M6 UX Enhancement).
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Loads the recipe image from the network using the Coil library (M4 Implementation).
                AsyncImage(
                    model = recipe.imageUrl,
                    // Content description is crucial for accessibility (M2 Accessibility).
                    contentDescription = "Image of ${recipe.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Adds a subtle gradient scrim over the bottom of the image for better contrast (M6 UX Enhancement).
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                startY = 0f,
                                endY = 200f
                            )
                        )
                )

                // The button controls the favorite status (M2 Feature).
                IconButton(
                    onClick = { onFavoriteClick(!recipe.isFavorite) },
                    // Adds a translucent background to the button for visual clarity (M6 UX Enhancement).
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                ) {
                    // Icon changes based on the recipe's state (M6 State Management).
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        // Uses the theme's primary color for the filled icon for thematic consistency (Maintenance Plan).
                        tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // A divider visually separates the image from the title area (M6 UX Enhancement).
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text(
                text = recipe.title,
                // Cursive font for unique branding (M2 UI/UX).
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