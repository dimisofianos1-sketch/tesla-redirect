package gr.posokanei4real.app.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import gr.posokanei4real.app.data.model.PriceEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onBack: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.item?.nameEl ?: "Προϊόν",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Πίσω")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header
            item {
                state.item?.let { item ->
                    Text(
                        text = item.nameEl,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Μονάδα: ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Τιμές ανά σούπερ μάρκετ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (state.prices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Δεν υπάρχουν διαθέσιμες τιμές ακόμα.\nΠατήστε Ανανέωση στην αρχική σελίδα.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Price cards sorted cheapest first
            val sorted = state.prices.sortedBy { it.price }
            val cheapest = sorted.firstOrNull()

            items(sorted, key = { it.supermarketId }) { entry ->
                PriceCard(
                    entry = entry,
                    isCheapest = entry.supermarketId == cheapest?.supermarketId,
                    onOpenUrl = { url ->
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun PriceCard(
    entry: PriceEntry,
    isCheapest: Boolean,
    onOpenUrl: (String) -> Unit,
) {
    val containerColor = if (isCheapest)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isCheapest) 4.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.supermarketName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (isCheapest) {
                        Spacer(Modifier.width(6.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Text("ΦΘΗΝΟΤΕΡΟ", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (entry.isOnSale && entry.originalPrice != null) {
                    Text(
                        text = "%.2f€".format(entry.originalPrice),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                entry.productNameFound?.takeIf { it.isNotBlank() }?.let { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f€".format(entry.price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCheapest)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
                entry.productUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    IconButton(
                        onClick = { onOpenUrl(url) },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = "Άνοιγμα στο κατάστημα",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
