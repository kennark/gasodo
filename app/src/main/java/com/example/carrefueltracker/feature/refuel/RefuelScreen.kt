package com.example.carrefueltracker.feature.refuel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

import com.example.carrefueltracker.core.database.entity.RefuelEvent
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.carrefueltracker.ui.icons.check_box
import com.example.carrefueltracker.ui.icons.check_box_outline_blank
import java.time.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import com.example.carrefueltracker.core.database.BaseColumns
import com.example.carrefueltracker.core.enums.PaymentMethod
import com.example.carrefueltracker.ui.icons.expand_circle_down

@Composable
fun RefuelScreen(
    viewModel: RefuelScreenViewModel = hiltViewModel<RefuelScreenViewModel>(),
) {
    val pagedItems = viewModel.pagedItems.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 15.dp),
            text = "Recent Refuels",
            style = MaterialTheme.typography.titleLarge
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                pagedItems.itemCount,
                key = pagedItems.itemKey { it.id }
            ) { index ->
                val refuel = pagedItems[index]
                if (refuel != null) {
                    RefuelEventRow(refuelEvent = refuel)
                } else {
                    // Show placeholder for empty slots while paging
                    RefuelScreenLoadingPlaceholder()
                }
            }
        }
    }
}

/**
 * Displays a single refuel event row/card.
 */
@Composable
fun RefuelEventRow(
    refuelEvent: RefuelEvent,
    modifier: Modifier = Modifier
) {
    val isExpanded = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /*// Date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.Left,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = calendar_today, contentDescription = calendar_today.name)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        // Convert the date from epoch milliseconds to epoch days
                        val dateString = LocalDate.ofEpochDay(refuelEvent.base.date?.div(86400000) ?: 0).toString()
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (refuelEvent.base.mileage != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = refuelEvent.base.mileage.toString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "km",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
            }

            */

            // Amount and Price row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    /*
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = calendar_today, contentDescription = calendar_today.name)
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                     */
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Convert the date from epoch milliseconds to epoch days
                        val dateString =
                            LocalDate.ofEpochDay(refuelEvent.base.date?.div(86400000) ?: 0)
                                .toString()
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (refuelEvent.base.mileage != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = refuelEvent.base.mileage.toString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "km",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                Column {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val amount = refuelEvent.amountLiters ?: 0.0
                    Text(
                        text = String.format(Locale.US, "%.2f L", amount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "Cost",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val total = refuelEvent.totalCost ?: 0.0
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "%.2f".format(total),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "€",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val price = refuelEvent.pricePerLiter ?: 0.0
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "%.3f".format(price),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "€/L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Full fill-up indicator row

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Full Fill-Up with icon together at the back (right)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Full Fill-Up",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    if (refuelEvent.fullFillUp) {
                        Icon(imageVector = check_box, contentDescription = check_box.name)
                    } else {
                        Icon(
                            imageVector = check_box_outline_blank,
                            contentDescription = check_box_outline_blank.name
                        )
                    }
                }
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded.value) 180f else 0f,
                    animationSpec = tween(durationMillis = 250)
                )
                // Expand button for showing additional fuel consumption info
                FilledTonalIconButton(
                    onClick = { isExpanded.value = !isExpanded.value },
                    modifier = Modifier.rotate(rotation)
                ) {
                    Icon(
                        imageVector = expand_circle_down,
                        contentDescription = if (isExpanded.value) "Collapse details" else "Expand details"
                    )
                }
            }


            // Expanded section with additional fuel consumption information
//            AnimatedVisibility(
//                visible = isExpanded.value,
//                enter = expandVertically(),
//                exit = shrinkVertically()
//            ) {
            if (isExpanded.value) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Fuel Consumption Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Mileage",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val currentMileage = refuelEvent.base.mileage ?: 0L
                            Text(
                                text = String.format(Locale.US, "%d km", currentMileage),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val dateString =
                                LocalDate.ofEpochDay(refuelEvent.base.date?.div(86400000) ?: 0)
                                    .toString()
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Liters",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val liters = refuelEvent.amountLiters ?: 0.0
                            Text(
                                text = String.format(Locale.US, "%.2f L", liters),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column {
                            Text(
                                text = "Cost",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val cost = refuelEvent.totalCost ?: 0.0
                            Text(
                                text = "%.2f €".format(cost),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Price per Liter",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val price = refuelEvent.pricePerLiter ?: 0.0
                            Text(
                                text = "%.3f €/L".format(price),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column {
                            Text(
                                text = "Payment",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = refuelEvent.paymentMethod?.toString() ?: "Unknown",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Full Fill-Up",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (refuelEvent.fullFillUp) "Yes" else "No",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        refuelEvent.base.notes.let { notes ->
                            if (notes.isNotEmpty()) {
                                Text(
                                    text = notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    text = "No notes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Placeholder for loading state.
 */
@Composable
fun RefuelScreenLoadingPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
    }
}


@Preview
@Composable
fun RefuelCardPreview() {
    RefuelEventRow(
        refuelEvent = RefuelEvent(
            base = BaseColumns(
                date = 1000000000000,
                mileage = 123543,

                ),
            amountLiters = 10.0,
            pricePerLiter = 1.599,
            totalCost = 12.22,
            paymentMethod = PaymentMethod.CARD,
            fullFillUp = true
        )
    )
}