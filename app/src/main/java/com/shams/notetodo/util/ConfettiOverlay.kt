import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun ConfettiOverlay() {
    val confettiCount = 20
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        repeat(confettiCount) {
            val xOffset = remember { Random.nextFloat() * 300 }
            val yOffset = remember { Random.nextFloat() * 300 }
            val size = remember { Random.nextInt(4, 8).dp }
            val color = remember {
                listOf(
                    Color(0xFFE91E63), // Pink
                    Color(0xFF2196F3), // Blue
                    Color(0xFFFFC107), // Amber
                    Color(0xFF4CAF50)  // Green
                ).random()
            }

            Box(
                modifier = Modifier
                    .offset(x = xOffset.dp, y = yOffset.dp)
                    .size(size)
                    .background(color = color, shape = RoundedCornerShape(50))
            )
        }
    }
}
