package com.example.tiketapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

/* ---------- Status pemesanan ---------- */
enum class OrderStatus { Idle, NameEmpty, Processing, Success }

private val BluePrimary = Color(0xFF1F5FCC)

private fun rupiah(value: Int): String =
    "Rp " + NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(value)

/* =====================================================================
   PARENT (STATEFUL)
   Semua state dikelola di sini: harga tiket, jumlah tiket, nama pembeli
   ===================================================================== */
@Composable
fun TicketApp() {
    // ===== 3 STATE UTAMA yang dikelola Parent =====
    val ticketPrice by rememberSaveable { mutableStateOf(50_000) }  // 1. Harga tiket
    var quantity by rememberSaveable { mutableStateOf(1) }          // 2. Jumlah tiket
    var buyerName by rememberSaveable { mutableStateOf("") }        // 3. Nama pembeli

    // ===== State pendukung =====
    var status by remember { mutableStateOf(OrderStatus.Idle) }
    var orderTrigger by remember { mutableStateOf(0) }   // key LaunchedEffect

    // ===== LaunchedEffect: jalan setiap tombol "Pesan Tiket" diklik =====
    LaunchedEffect(orderTrigger) {
        if (orderTrigger == 0) return@LaunchedEffect   // abaikan saat pertama tampil

        if (buyerName.isBlank()) {
            status = OrderStatus.NameEmpty             // Status: Nama masih kosong
        } else {
            status = OrderStatus.Processing            // Status: Memproses pesanan...
            delay(5_000)                               // tunggu 5 detik
            status = OrderStatus.Success               // Status: Tiket telah dipesan
        }
    }

    // Parent mengirim STATE ke bawah, menerima EVENT dari bawah
    TicketScreen(
        ticketPrice = ticketPrice,
        quantity = quantity,
        buyerName = buyerName,
        status = status,
        onNameChange = {
            buyerName = it
            if (status != OrderStatus.Processing) status = OrderStatus.Idle
        },
        onDecrease = { if (quantity > 1) quantity-- },
        onIncrease = { if (quantity < 10) quantity++ },
        onOrderClick = { orderTrigger++ }
    )
}

/* =====================================================================
   CHILD (STATELESS)
   Tidak menyimpan state; hanya menampilkan data & memanggil callback
   ===================================================================== */
@Composable
fun TicketScreen(
    ticketPrice: Int,
    quantity: Int,
    buyerName: String,
    status: OrderStatus,
    onNameChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onOrderClick: () -> Unit
) {
    val isProcessing = status == OrderStatus.Processing

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        // ----- Header biru -----
        Box(
            Modifier
                .fillMaxWidth()
                .background(BluePrimary)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                "Pemesanan Tiket",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // ----- Isi form -----
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Nama
            Text("Nama", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = buyerName,
                onValueChange = onNameChange,
                placeholder = { Text("Masukkan nama Anda") },
                singleLine = true,
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            )

            // Jumlah tiket
            Spacer(Modifier.height(16.dp))
            Text("Jumlah Tiket", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onDecrease,
                    enabled = !isProcessing && quantity > 1,
                    modifier = Modifier.width(96.dp)
                ) { Text("-") }

                Text("$quantity", fontSize = 28.sp, fontWeight = FontWeight.Bold)

                FilledTonalButton(
                    onClick = onIncrease,
                    enabled = !isProcessing,
                    modifier = Modifier.width(96.dp)
                ) { Text("+") }
            }

            // Harga & total
            Spacer(Modifier.height(12.dp))
            Text(
                "Harga: ${rupiah(ticketPrice)} / tiket",
                color = Color.DarkGray,
                fontSize = 14.sp
            )
            Text(
                "Total: ${rupiah(ticketPrice * quantity)}",
                fontWeight = FontWeight.SemiBold
            )

            // Tombol pesan
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onOrderClick,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("Pesan Tiket") }

            // Kartu status
            Spacer(Modifier.height(16.dp))
            StatusCard(status)
        }
    }
}

/* ---------- Ikon bulat sederhana (pengganti material-icons) ---------- */
@Composable
private fun StatusIcon(symbol: String, color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ---------- Kartu status (berubah sesuai OrderStatus) ---------- */
@Composable
private fun StatusCard(status: OrderStatus) {
    val (bg, fg, message) = when (status) {
        OrderStatus.Idle ->
            Triple(Color(0xFFEAF1FB), Color(0xFF37474F), "Status: Silakan pesan tiket")
        OrderStatus.NameEmpty ->
            Triple(Color(0xFFFDE8E8), Color(0xFFC62828), "Status: Nama masih kosong")
        OrderStatus.Processing ->
            Triple(Color(0xFFE3F0FF), Color(0xFF1F5FCC), "Status: Memproses pesanan...")
        OrderStatus.Success ->
            Triple(Color(0xFFE6F6EA), Color(0xFF2E7D32), "Status: Tiket telah dipesan!")
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (status) {
                OrderStatus.Processing ->
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = fg
                    )
                OrderStatus.Success -> StatusIcon("✓", fg)
                OrderStatus.NameEmpty -> StatusIcon("!", fg)
                OrderStatus.Idle -> StatusIcon("i", fg)
            }
            Spacer(Modifier.width(12.dp))
            Text(message, color = fg, fontWeight = FontWeight.Medium)
        }
    }
}