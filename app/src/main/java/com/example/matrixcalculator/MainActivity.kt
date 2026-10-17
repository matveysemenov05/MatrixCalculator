package com.example.matrixcalculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

val BgDark = Color(0xFF090D16)
val CardDark = Color(0xFF131D2E)
val AccentCyan = Color(0xFF06B6D4)
val AccentPurple = Color(0xFF8B5CF6)
val AccentAmber = Color(0xFFF59E0B)
val AccentEmerald = Color(0xFF10B981)
val TextWhite = Color(0xFFF1F5F9)
val TextDim = Color(0xFF64748B)

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val resultText: String,
    val resultMatrix: Array<DoubleArray>?,
    val time: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainAppSwitcher()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppSwitcher() {
    var appMode by rememberSaveable { mutableIntStateOf(0) }
    val historyList = remember { mutableStateListOf<HistoryItem>() }
    var showHistorySheet by remember { mutableStateOf(false) }

    // --- КВАДРАТНЫЕ МАТРИЦЫ (Живут вечно, не сбрасываются) ---
    var squareSize by rememberSaveable { mutableIntStateOf(3) }
    var squareSelectedTab by rememberSaveable { mutableIntStateOf(0) }
    var squareMatrixA by rememberSaveable { mutableStateOf(List(3) { List(3) { "0" } }) }
    var squareMatrixB by rememberSaveable { mutableStateOf(List(3) { List(3) { "0" } }) }
    var squareMatrixC by rememberSaveable { mutableStateOf(List(3) { List(3) { "0" } }) }
    var squareScalarK by rememberSaveable { mutableStateOf("2") }

    // --- ПРЯМОУГОЛЬНЫЕ МАТРИЦЫ (Живут вечно, не сбрасываются) ---
    var rectSelectedTab by rememberSaveable { mutableIntStateOf(0) }
    var rectRowsA by rememberSaveable { mutableIntStateOf(2) }
    var rectColsA by rememberSaveable { mutableIntStateOf(3) }
    var rectMatrixA by rememberSaveable { mutableStateOf(List(2) { List(3) { "0" } }) }

    var rectRowsB by rememberSaveable { mutableIntStateOf(3) }
    var rectColsB by rememberSaveable { mutableIntStateOf(2) }
    var rectMatrixB by rememberSaveable { mutableStateOf(List(3) { List(2) { "0" } }) }
    var rectScalarK by rememberSaveable { mutableStateOf("2") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDark, Color(0xFF030712))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Шапка и кнопка истории
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MATRIX LAB",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "Theory of Control Edition",
                        fontSize = 11.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FilledTonalButton(
                    onClick = { showHistorySheet = true },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = CardDark),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🕒 ${historyList.size}",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Табы переключения режима
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .padding(5.dp)
            ) {
                TabButton(
                    text = "Квадратные (A, B, C)",
                    selected = appMode == 0,
                    modifier = Modifier.weight(1f)
                ) { appMode = 0 }

                TabButton(
                    text = "Прямоугольные / Векторы",
                    selected = appMode == 1,
                    modifier = Modifier.weight(1f)
                ) { appMode = 1 }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = appMode,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width / 2 } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> -width / 2 } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width / 2 } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> width / 2 } + fadeOut())
                    }
                },
                label = "ScreenTransition"
            ) { mode ->
                if (mode == 0) {
                    SquareMatricesScreen(
                        size = squareSize,
                        onSizeChange = { newSize ->
                            squareSize = newSize
                            squareMatrixA = resizeMatrix(squareMatrixA, newSize, newSize)
                            squareMatrixB = resizeMatrix(squareMatrixB, newSize, newSize)
                            squareMatrixC = resizeMatrix(squareMatrixC, newSize, newSize)
                        },
                        selectedTab = squareSelectedTab,
                        onTabChange = { squareSelectedTab = it },
                        matrixA = squareMatrixA,
                        onMatrixAChange = { squareMatrixA = it },
                        matrixB = squareMatrixB,
                        onMatrixBChange = { squareMatrixB = it },
                        matrixC = squareMatrixC,
                        onMatrixCChange = { squareMatrixC = it },
                        scalarK = squareScalarK,
                        onScalarKChange = { squareScalarK = it },
                        onAddToHistory = { historyList.add(0, it) }
                    )
                } else {
                    RectangularMatricesScreen(
                        selectedTab = rectSelectedTab,
                        onTabChange = { rectSelectedTab = it },
                        rowsA = rectRowsA,
                        colsA = rectColsA,
                        onSizeAChange = { r, c ->
                            rectRowsA = r; rectColsA = c
                            rectMatrixA = resizeMatrix(rectMatrixA, r, c)
                        },
                        matrixA = rectMatrixA,
                        onMatrixAChange = { rectMatrixA = it },
                        rowsB = rectRowsB,
                        colsB = rectColsB,
                        onSizeBChange = { r, c ->
                            rectRowsB = r; rectColsB = c
                            rectMatrixB = resizeMatrix(rectMatrixB, r, c)
                        },
                        matrixB = rectMatrixB,
                        onMatrixBChange = { rectMatrixB = it },
                        scalarK = rectScalarK,
                        onScalarKChange = { rectScalarK = it },
                        onAddToHistory = { historyList.add(0, it) }
                    )
                }
            }
        }

        // ШТОРКА ИСТОРИИ
        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                containerColor = CardDark,
                scrimColor = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                HistoryBottomSheetContent(
                    historyList = historyList,
                    onClearHistory = { historyList.clear() },
                    onPasteToSquareA = {
                        val n = it.size
                        squareSize = n
                        squareMatrixA = matrixToStringList(it)
                        showHistorySheet = false
                    },
                    onPasteToSquareB = {
                        val n = it.size
                        squareSize = n
                        squareMatrixB = matrixToStringList(it)
                        showHistorySheet = false
                    },
                    onPasteToSquareC = {
                        val n = it.size
                        squareSize = n
                        squareMatrixC = matrixToStringList(it)
                        showHistorySheet = false
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// РЕЖИМ 1: КВАДРАТНЫЕ МАТРИЦЫ (A, B, C)
// -------------------------------------------------------------
@Composable
fun SquareMatricesScreen(
    size: Int,
    onSizeChange: (Int) -> Unit,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    matrixA: List<List<String>>,
    onMatrixAChange: (List<List<String>>) -> Unit,
    matrixB: List<List<String>>,
    onMatrixBChange: (List<List<String>>) -> Unit,
    matrixC: List<List<String>>,
    onMatrixCChange: (List<List<String>>) -> Unit,
    scalarK: String,
    onScalarKChange: (String) -> Unit,
    onAddToHistory: (HistoryItem) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val math = remember { MatrixMath() }
    val screenWidth = configuration.screenWidthDp.dp

    var lastResultMatrix by remember { mutableStateOf<Array<DoubleArray>?>(null) }
    var resultScalarText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val currentData = when (selectedTab) { 0 -> matrixA 1 -> matrixB else -> matrixC }
    val currentAccent = when (selectedTab) { 0 -> AccentCyan 1 -> AccentPurple else -> AccentAmber }
    val tabName = when (selectedTab) { 0 -> "A" 1 -> "B" else -> "C" }

    fun updateCurrent(updated: List<List<String>>) {
        when (selectedTab) {
            0 -> onMatrixAChange(updated)
            1 -> onMatrixBChange(updated)
            else -> onMatrixCChange(updated)
        }
    }

    val calculatedCellSize = ((screenWidth - 48.dp) / size).coerceIn(48.dp, 64.dp)
    val cellFontSize = if (calculatedCellSize < 54.dp) 13.sp else 16.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Табы A, B, C и кнопки размера
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardDark)
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)),
                containerColor = Color.Transparent,
                indicator = {},
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { onTabChange(0) }, text = { Text("A", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) AccentCyan else TextDim, fontSize = 15.sp) })
                Tab(selected = selectedTab == 1, onClick = { onTabChange(1) }, text = { Text("B", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) AccentPurple else TextDim, fontSize = 15.sp) })
                Tab(selected = selectedTab == 2, onClick = { onTabChange(2) }, text = { Text("C", fontWeight = FontWeight.Bold, color = if (selectedTab == 2) AccentAmber else TextDim, fontSize = 15.sp) })
            }

            Spacer(modifier = Modifier.width(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                CounterButton(symbol = "−", enabled = size > 2, onClick = { onSizeChange(size - 1) })
                Text(text = "${size}×$size", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 10.dp))
                CounterButton(symbol = "+", enabled = size < 5, onClick = { onSizeChange(size + 1) })
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Сетка матрицы
        MatrixGridInput(
            rows = size,
            cols = size,
            data = currentData,
            accentColor = currentAccent,
            cellSize = calculatedCellSize,
            fontSize = cellFontSize,
            onValueChange = { r, c, v ->
                val updated = currentData.mapIndexed { ri, row ->
                    if (ri == r) row.mapIndexed { ci, cur -> if (ci == c) v else cur } else row
                }
                updateCurrent(updated)
            }
        )

        // Кнопки: Рандом / ВСТАВИТЬ / Очистить
        Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = { updateCurrent(List(size) { List(size) { Random.nextInt(-9, 10).toString() } }) },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("Рандом", color = TextWhite, fontSize = 11.sp) }

            // ВОТ ОНА: Кнопка ВСТАВИТЬ из буфера обмена!
            FilledTonalButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                    if (!clipText.isNullOrEmpty()) {
                        val parsed = parsePastedText(clipText, size, size)
                        if (parsed != null) {
                            updateCurrent(parsed)
                            Toast.makeText(context, "Вставлено в матрицу $tabName!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Не удалось распознать матрицу из текста!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Буфер обмена пуст!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = AccentCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("Вставить", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

            FilledTonalButton(
                onClick = { updateCurrent(List(size) { List(size) { "0" } }) },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("Очистить", color = Color(0xFFF87171), fontSize = 11.sp) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Тройные операции
        Text("Тройные операции (A, B, C):", color = TextDim, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AdaptiveBtn("A + B + C", Modifier.weight(1f), highlight = true) {
                val a = parse(matrixA); val b = parse(matrixB); val c = parse(matrixC)
                val ab = math.add(a, b); val res = if (ab != null) math.add(ab, c) else null
                lastResultMatrix = res; resultScalarText = ""; isError = res == null
                if (res != null) onAddToHistory(HistoryItem(title = "A + B + C", resultText = formatMatrixRaw(res), resultMatrix = res))
            }
            AdaptiveBtn("A - B - C", Modifier.weight(1f), highlight = true) {
                val a = parse(matrixA); val b = parse(matrixB); val c = parse(matrixC)
                val ab = math.subtract(a, b); val res = if (ab != null) math.subtract(ab, c) else null
                lastResultMatrix = res; resultScalarText = ""; isError = res == null
                if (res != null) onAddToHistory(HistoryItem(title = "A - B - C", resultText = formatMatrixRaw(res), resultMatrix = res))
            }
            AdaptiveBtn("A × B × C", Modifier.weight(1f), highlight = true) {
                val a = parse(matrixA); val b = parse(matrixB); val c = parse(matrixC)
                val ab = math.multiply(a, b); val res = if (ab != null) math.multiply(ab, c) else null
                lastResultMatrix = res; resultScalarText = ""; isError = res == null
                if (res != null) onAddToHistory(HistoryItem(title = "A × B × C", resultText = formatMatrixRaw(res), resultMatrix = res))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Операции A и B
        Text("Операции A и B:", color = TextDim, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AdaptiveBtn("A + B", Modifier.weight(1f)) {
                val res = math.add(parse(matrixA), parse(matrixB))
                lastResultMatrix = res; resultScalarText = ""; isError = false
                if (res != null) onAddToHistory(HistoryItem(title = "A + B", resultText = formatMatrixRaw(res), resultMatrix = res))
            }
            AdaptiveBtn("A - B", Modifier.weight(1f)) {
                val res = math.subtract(parse(matrixA), parse(matrixB))
                lastResultMatrix = res; resultScalarText = ""; isError = false
                if (res != null) onAddToHistory(HistoryItem(title = "A - B", resultText = formatMatrixRaw(res), resultMatrix = res))
            }
            AdaptiveBtn("A × B", Modifier.weight(1f)) {
                val res = math.multiply(parse(matrixA), parse(matrixB))
                lastResultMatrix = res; resultScalarText = ""; isError = false
                if (res != null) onAddToHistory(HistoryItem(title = "A × B", resultText = formatMatrixRaw(res), resultMatrix = res))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Операции над выбранной
        Text("Операции над [$tabName]:", color = TextDim, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AdaptiveBtn("${tabName}⁻¹ (Обратная)", Modifier.weight(1f)) {
                val res = math.inverse(parse(currentData))
                if (res != null) {
                    lastResultMatrix = res; resultScalarText = ""; isError = false
                    onAddToHistory(HistoryItem(title = "Обратная $tabName⁻¹", resultText = formatMatrixRaw(res), resultMatrix = res))
                } else {
                    lastResultMatrix = null; resultScalarText = "Определитель равен 0!\nОбратная матрица не существует."; isError = true
                }
            }
            AdaptiveBtn("${tabName}ᵀ (Трансп.)", Modifier.weight(1f)) {
                val res = math.transpose(parse(currentData))
                lastResultMatrix = res; resultScalarText = ""; isError = false
                onAddToHistory(HistoryItem(title = "Транспонирование ${tabName}ᵀ", resultText = formatMatrixRaw(res), resultMatrix = res))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Аналитика ТАУ
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AdaptiveBtn("det($tabName)", Modifier.weight(1f)) {
                lastResultMatrix = null
                val res = math.determinant(parse(currentData))
                val txt = "det($tabName) = ${formatNumber(res ?: 0.0)}"
                resultScalarText = "Определитель:\n$txt"; isError = false
                onAddToHistory(HistoryItem(title = "Определитель det($tabName)", resultText = txt, resultMatrix = null))
            }
            AdaptiveBtn("Ранг", Modifier.weight(1f)) {
                lastResultMatrix = null
                val res = math.rank(parse(currentData))
                val txt = "rank($tabName) = $res"
                resultScalarText = "Ранг матрицы (Rank):\n$txt"; isError = false
                onAddToHistory(HistoryItem(title = "Ранг rank($tabName)", resultText = txt, resultMatrix = null))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AdaptiveBtn("След Tr($tabName)", Modifier.weight(1f)) {
                lastResultMatrix = null
                val res = math.trace(parse(currentData))
                val txt = "Tr($tabName) = ${formatNumber(res ?: 0.0)}"
                resultScalarText = "След матрицы (Trace):\n$txt"; isError = false
                onAddToHistory(HistoryItem(title = "След Tr($tabName)", resultText = txt, resultMatrix = null))
            }
            AdaptiveBtn("‖$tabName‖_F Норма", Modifier.weight(1f), highlight = true) {
                lastResultMatrix = null
                val norm = math.norm(parse(currentData))
                val txt = "‖$tabName‖_F = ${formatNumber(norm)}"
                resultScalarText = "Норма Фробениуса:\n$txt"; isError = false
                onAddToHistory(HistoryItem(title = "Норма Фробениуса ‖$tabName‖_F", resultText = txt, resultMatrix = null))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Скаляр k
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Скаляр k:", color = TextWhite, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF24334D), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = scalarK,
                    onValueChange = onScalarKChange,
                    textStyle = TextStyle(color = TextWhite, textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(AccentCyan)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val k = scalarK.toDoubleOrNull() ?: 1.0
                    val res = math.multiplyScalar(parse(currentData), k)
                    lastResultMatrix = res; resultScalarText = ""; isError = false
                    onAddToHistory(HistoryItem(title = "Умножение $k × $tabName", resultText = formatMatrixRaw(res), resultMatrix = res))
                },
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("k × $tabName", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Результат
        ResultCardBlock(
            lastResultMatrix = lastResultMatrix,
            resultScalarText = resultScalarText,
            isError = isError,
            screenWidth = screenWidth,
            context = context,
            onTransferToA = { onMatrixAChange(matrixToStringList(it)) },
            onTransferToB = { onMatrixBChange(matrixToStringList(it)) },
            onTransferToC = { onMatrixCChange(matrixToStringList(it)) }
        )
    }
}

// -------------------------------------------------------------
// РЕЖИМ 2: ПРЯМОУГОЛЬНЫЕ МАТРИЦЫ И ВЕКТОРЫ
// -------------------------------------------------------------
@Composable
fun RectangularMatricesScreen(
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    rowsA: Int,
    colsA: Int,
    onSizeAChange: (Int, Int) -> Unit,
    matrixA: List<List<String>>,
    onMatrixAChange: (List<List<String>>) -> Unit,
    rowsB: Int,
    colsB: Int,
    onSizeBChange: (Int, Int) -> Unit,
    matrixB: List<List<String>>,
    onMatrixBChange: (List<List<String>>) -> Unit,
    scalarK: String,
    onScalarKChange: (String) -> Unit,
    onAddToHistory: (HistoryItem) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val math = remember { MatrixMath() }
    val screenWidth = configuration.screenWidthDp.dp

    var lastResultMatrix by remember { mutableStateOf<Array<DoubleArray>?>(null) }
    var resultScalarText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val curRows = if (selectedTab == 0) rowsA else rowsB
    val curCols = if (selectedTab == 0) colsA else colsB
    val currentData = if (selectedTab == 0) matrixA else matrixB
    val currentAccent = if (selectedTab == 0) AccentCyan else AccentPurple
    val tabName = if (selectedTab == 0) "A" else "B"

    fun updateCurrent(updated: List<List<String>>) {
        if (selectedTab == 0) onMatrixAChange(updated) else onMatrixBChange(updated)
    }

    val calculatedCellSize = ((screenWidth - 48.dp) / curCols).coerceIn(48.dp, 64.dp)
    val cellFontSize = if (calculatedCellSize < 54.dp) 13.sp else 16.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Выбор Матрицы A / B и кнопка смены местами
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .padding(4.dp)
            ) {
                TabButton("Матрица A (${rowsA}×$colsA)", selectedTab == 0, Modifier.weight(1f)) { onTabChange(0) }
                TabButton("Матрица B (${rowsB}×$colsB)", selectedTab == 1, Modifier.weight(1f)) { onTabChange(1) }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = {
                    val tempMat = matrixA; val tempR = rowsA; val tempC = colsA
                    onMatrixAChange(matrixB); onSizeAChange(rowsB, colsB)
                    onMatrixBChange(tempMat); onSizeBChange(tempR, tempC)
                    Toast.makeText(context, "A ⇄ B поменяны местами", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text("A ⇄ B", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Настройка строк и столбцов
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Строк (M)", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        CounterButton(symbol = "−", enabled = curRows > 1, onClick = {
                            if (selectedTab == 0) onSizeAChange(rowsA - 1, colsA) else onSizeBChange(rowsB - 1, colsB)
                        })
                        Text(text = "$curRows", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp))
                        CounterButton(symbol = "+", enabled = curRows < 6, onClick = {
                            if (selectedTab == 0) onSizeAChange(rowsA + 1, colsA) else onSizeBChange(rowsB + 1, colsB)
                        })
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Столбцов (N)", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        CounterButton(symbol = "−", enabled = curCols > 1, onClick = {
                            if (selectedTab == 0) onSizeAChange(rowsA, colsA - 1) else onSizeBChange(rowsB, colsB - 1)
                        })
                        Text(text = "$curCols", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp))
                        CounterButton(symbol = "+", enabled = curCols < 6, onClick = {
                            if (selectedTab == 0) onSizeAChange(rowsA, colsA + 1) else onSizeBChange(rowsB, colsB + 1)
                        })
                    }
                }
            }
        }

        // Векторные пресеты
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = { if (selectedTab == 0) onSizeAChange(1, 3) else onSizeBChange(1, 3) },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("➔ Вектор-строка (1×3)", color = AccentCyan, fontSize = 11.sp) }

            FilledTonalButton(
                onClick = { if (selectedTab == 0) onSizeAChange(3, 1) else onSizeBChange(3, 1) },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("➔ Вектор-столбец (3×1)", color = AccentPurple, fontSize = 11.sp) }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Сетка ввода
        MatrixGridInput(
            rows = curRows,
            cols = curCols,
            data = currentData,
            accentColor = currentAccent,
            cellSize = calculatedCellSize,
            fontSize = cellFontSize,
            onValueChange = { r, c, v ->
                val updated = currentData.mapIndexed { ri, row ->
                    if (ri == r) row.mapIndexed { ci, cur -> if (ci == c) v else cur } else row
                }
                updateCurrent(updated)
            }
        )

        // Кнопки: Рандом / ВСТАВИТЬ / Очистить
        Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = { updateCurrent(List(curRows) { List(curCols) { Random.nextInt(-9, 10).toString() } }) },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("Рандом", color = TextWhite, fontSize = 11.sp) }

            FilledTonalButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                    if (!clipText.isNullOrEmpty()) {
                        val parsed = parsePastedText(clipText, curRows, curCols)
                        if (parsed != null) {
                            updateCurrent(parsed)
                            Toast.makeText(context, "Вставлено в матрицу $tabName!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Не удалось распознать матрицу из текста!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Буфер обмена пуст!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = AccentCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("Вставить", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

            FilledTonalButton(
                onClick = { updateCurrent(List(curRows) { List(curCols) { "0" } }) },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("Очистить", color = Color(0xFFF87171), fontSize = 11.sp) }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Операции A и B
        Text("Матричные операции над A и B:", color = TextDim, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AdaptiveBtn("A × B", Modifier.weight(1f), highlight = true) {
                if (colsA != rowsB) {
                    lastResultMatrix = null
                    resultScalarText = "Нельзя умножить!\nЧисло столбцов A ($colsA) должно совпадать с числом строк B ($rowsB)."
                    isError = true
                } else {
                    val res = math.multiply(parse(matrixA), parse(matrixB))
                    lastResultMatrix = res; resultScalarText = ""; isError = false
                    if (res != null) onAddToHistory(HistoryItem(title = "Умножение A × B", resultText = formatMatrixRaw(res), resultMatrix = res))
                }
            }
            AdaptiveBtn("A + B", Modifier.weight(1f)) {
                if (rowsA != rowsB || colsA != colsB) {
                    lastResultMatrix = null
                    resultScalarText = "Нельзя сложить!\nРазмеры матриц не совпадают."
                    isError = true
                } else {
                    val res = math.add(parse(matrixA), parse(matrixB))
                    lastResultMatrix = res; resultScalarText = ""; isError = false
                    if (res != null) onAddToHistory(HistoryItem(title = "Сложение A + B", resultText = formatMatrixRaw(res), resultMatrix = res))
                }
            }
            AdaptiveBtn("A - B", Modifier.weight(1f)) {
                if (rowsA != rowsB || colsA != colsB) {
                    lastResultMatrix = null
                    resultScalarText = "Нельзя вычесть!\nРазмеры матриц не совпадают."
                    isError = true
                } else {
                    val res = math.subtract(parse(matrixA), parse(matrixB))
                    lastResultMatrix = res; resultScalarText = ""; isError = false
                    if (res != null) onAddToHistory(HistoryItem(title = "Вычитание A - B", resultText = formatMatrixRaw(res), resultMatrix = res))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Векторные операции
        Text("Векторные операции (A и B как векторы):", color = TextDim, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AdaptiveBtn("a · b (Скалярное)", Modifier.weight(1f)) {
                val totalA = rowsA * colsA; val totalB = rowsB * colsB
                if (totalA != totalB) {
                    lastResultMatrix = null
                    resultScalarText = "Ошибка! У векторов должно быть равное количество чисел ($totalA ≠ $totalB)."
                    isError = true
                } else {
                    val dot = math.dotProduct(parse(matrixA), parse(matrixB))
                    val txt = "a · b = ${formatNumber(dot ?: 0.0)}"
                    lastResultMatrix = null; resultScalarText = "Скалярное произведение:\n$txt"; isError = false
                    onAddToHistory(HistoryItem(title = "Скалярное произведение a · b", resultText = txt, resultMatrix = null))
                }
            }

            AdaptiveBtn("a ⨯ b (Векторное 3D)", Modifier.weight(1f)) {
                val totalA = rowsA * colsA; val totalB = rowsB * colsB
                if (totalA != 3 || totalB != 3) {
                    lastResultMatrix = null
                    resultScalarText = "Векторное произведение существует только для 3D векторов!"
                    isError = true
                } else {
                    val cross = math.crossProduct(parse(matrixA), parse(matrixB))
                    lastResultMatrix = cross; resultScalarText = ""; isError = false
                    if (cross != null) onAddToHistory(HistoryItem(title = "Векторное произведение a ⨯ b", resultText = formatMatrixRaw(cross), resultMatrix = cross))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Анализ
        Text("Анализ и свойства для [$tabName]:", color = TextDim, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AdaptiveBtn("${tabName}ᵀ (Транспонировать)", Modifier.weight(1f)) {
                val res = math.transpose(parse(currentData))
                lastResultMatrix = res; resultScalarText = ""; isError = false
                onAddToHistory(HistoryItem(title = "Транспонирование ${tabName}ᵀ", resultText = formatMatrixRaw(res), resultMatrix = res))
            }
            AdaptiveBtn("Ранг ($tabName)", Modifier.weight(1f)) {
                val res = math.rank(parse(currentData))
                val txt = "rank($tabName) = $res"
                lastResultMatrix = null; resultScalarText = "Ранг матрицы ($tabName) = $res"; isError = false
                onAddToHistory(HistoryItem(title = "Ранг матрицы rank($tabName)", resultText = txt, resultMatrix = null))
            }
            AdaptiveBtn("Норма ‖$tabName‖", Modifier.weight(1f)) {
                val norm = math.norm(parse(currentData))
                val txt = "‖$tabName‖ = ${formatNumber(norm)}"
                lastResultMatrix = null; resultScalarText = "Длина вектора / Норма:\n$txt"; isError = false
                onAddToHistory(HistoryItem(title = "Норма ‖$tabName‖", resultText = txt, resultMatrix = null))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Скаляр k
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Скаляр k:", color = TextWhite, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF24334D), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = scalarK,
                    onValueChange = onScalarKChange,
                    textStyle = TextStyle(color = TextWhite, textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(AccentCyan)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val k = scalarK.toDoubleOrNull() ?: 1.0
                    val res = math.multiplyScalar(parse(currentData), k)
                    lastResultMatrix = res; resultScalarText = ""; isError = false
                    onAddToHistory(HistoryItem(title = "Умножение $k × $tabName", resultText = formatMatrixRaw(res), resultMatrix = res))
                },
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("k × $tabName", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        ResultCardBlock(
            lastResultMatrix = lastResultMatrix,
            resultScalarText = resultScalarText,
            isError = isError,
            screenWidth = screenWidth,
            context = context,
            onTransferToA = {
                onSizeAChange(it.size, it[0].size)
                onMatrixAChange(matrixToStringList(it))
            },
            onTransferToB = {
                onSizeBChange(it.size, it[0].size)
                onMatrixBChange(matrixToStringList(it))
            },
            onTransferToC = null
        )
    }
}

// -------------------------------------------------------------
// ШТОРКА ИСТОРИИ (С КНОПКАМИ БЫСТРОГО ПЕРЕНОСА)
// -------------------------------------------------------------
@Composable
fun HistoryBottomSheetContent(
    historyList: List<HistoryItem>,
    onClearHistory: () -> Unit,
    onPasteToSquareA: (Array<DoubleArray>) -> Unit,
    onPasteToSquareB: (Array<DoubleArray>) -> Unit,
    onPasteToSquareC: (Array<DoubleArray>) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "История вычислений",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Всего записей: ${historyList.size}",
                    color = TextDim,
                    fontSize = 12.sp
                )
            }

            if (historyList.isNotEmpty()) {
                FilledTonalButton(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF450A0A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Очистить", color = Color(0xFFFCA5A5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "История пуста.\nВыполните любую операцию!",
                    color = TextDim,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList.size) { index ->
                    val item = historyList[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Заголовок и копирование
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.time,
                                        color = TextDim,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "📋",
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                val txt = if (item.resultMatrix != null) formatMatrixRaw(item.resultMatrix) else item.resultText
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Matrix Result", txt)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Скопировано в буфер!", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Отрисовка: матрица или текст
                            if (item.resultMatrix != null) {
                                HistoryMatrixGrid(item.resultMatrix)

                                // Кнопки быстрого встраивания в A, B или C прямо из истории!
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                                        contentPadding = PaddingValues(0.dp),
                                        onClick = { onPasteToSquareA(item.resultMatrix) }
                                    ) { Text("➔ в A", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

                                    Button(
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple),
                                        contentPadding = PaddingValues(0.dp),
                                        onClick = { onPasteToSquareB(item.resultMatrix) }
                                    ) { Text("➔ в B", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

                                    Button(
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber),
                                        contentPadding = PaddingValues(0.dp),
                                        onClick = { onPasteToSquareC(item.resultMatrix) }
                                    ) { Text("➔ в C", color = AccentAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                }
                            } else {
                                Text(
                                    text = item.resultText,
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// -------------------------------------------------------------
// ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ И ФУНКЦИИ
// -------------------------------------------------------------
@Composable
fun HistoryMatrixGrid(matrix: Array<DoubleArray>) {
    val rows = matrix.size
    val cols = matrix[0].size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height((rows * 26).dp)
                .width(3.dp)
                .background(AccentEmerald, RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))

        Column {
            for (i in 0 until rows) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (j in 0 until cols) {
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(26.dp)
                                .padding(horizontal = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatNumber(matrix[i][j]),
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .height((rows * 26).dp)
                .width(3.dp)
                .background(AccentEmerald, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
        )
    }
}

@Composable
fun CounterButton(
    symbol: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "counterScale"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = if (enabled) Color(0xFF334155) else Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (enabled) TextWhite else TextDim.copy(alpha = 0.3f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AccentCyan.copy(alpha = 0.2f) else Color.Transparent)
            .border(if (selected) 1.dp else 0.dp, if (selected) AccentCyan else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) AccentCyan else TextDim,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MatrixGridInput(
    rows: Int,
    cols: Int,
    data: List<List<String>>,
    accentColor: Color,
    cellSize: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit, // базовый размер
    onValueChange: (Int, Int, String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Левая скобка [
        Box(
            modifier = Modifier
                .height((rows * cellSize.value * 0.95f).dp)
                .width(4.dp)
                .background(accentColor, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))

        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f)
            )
        ) {
            for (i in 0 until rows) {
                Row(modifier = Modifier.animateContentSize()) {
                    for (j in 0 until cols) {
                        val cellVal = if (i < data.size && j < data[i].size) data[i][j] else "0"
                        var isFocused by remember { mutableStateOf(false) }

                        // Динамический расчет размера шрифта: длинные дроби уменьшаем!
                        val dynamicFontSize = when {
                            cellVal.length > 5 -> 10.5.sp
                            cellVal.length > 3 -> 12.5.sp
                            else -> 15.sp
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .padding(2.5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardDark)
                                .border(
                                    width = if (isFocused) 1.5.dp else 1.dp,
                                    color = if (isFocused) accentColor else Color(0xFF24334D),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                    if (focusState.isFocused && cellVal == "0") {
                                        onValueChange(i, j, "")
                                    } else if (!focusState.isFocused && cellVal.isEmpty()) {
                                        onValueChange(i, j, "0")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = cellVal,
                                onValueChange = { input: String ->
                                    if (input.isEmpty() || input.matches(Regex("^-?\\d*(\\.\\d*)?$"))) {
                                        onValueChange(i, j, input)
                                    }
                                },
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = dynamicFontSize,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                cursorBrush = SolidColor(accentColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp) // Минимальные отступы чтобы всё влезало!
                            )

                            // Плейсхолдер если пусто
                            if (cellVal.isEmpty()) {
                                Text(
                                    text = "0",
                                    color = TextDim.copy(alpha = 0.4f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))
        // Правая скобка ]
        Box(
            modifier = Modifier
                .height((rows * cellSize.value * 0.95f).dp)
                .width(4.dp)
                .background(accentColor, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
        )
    }
}

@Composable
fun ResultCardBlock(
    lastResultMatrix: Array<DoubleArray>?,
    resultScalarText: String,
    isError: Boolean,
    screenWidth: Dp,
    context: Context,
    onTransferToA: (Array<DoubleArray>) -> Unit,
    onTransferToB: (Array<DoubleArray>) -> Unit,
    onTransferToC: ((Array<DoubleArray>) -> Unit)? = null
) {
    val hasResult = lastResultMatrix != null || resultScalarText.isNotEmpty()
    AnimatedVisibility(visible = hasResult, enter = fadeIn() + slideInVertically()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isError) Color(0xFF3B0B0B) else CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isError) Color(0xFFEF4444) else AccentEmerald)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isError) "Ошибка" else "Результат:",
                        color = if (isError) Color(0xFFFCA5A5) else AccentEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    TextButton(
                        onClick = {
                            val txt = if (lastResultMatrix != null) formatMatrixRaw(lastResultMatrix) else resultScalarText
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Matrix Result", txt)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Скопировано в буфер!", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Копировать", color = TextDim, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (resultScalarText.isNotEmpty()) {
                    Text(
                        text = resultScalarText,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 22.sp
                    )
                } else if (lastResultMatrix != null) {
                    MatrixResultGridAdaptive(lastResultMatrix, screenWidth)
                }

                if (lastResultMatrix != null && !isError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color(0xFF24334D), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Перенести ответ в:", color = TextDim, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                onTransferToA(lastResultMatrix)
                                Toast.makeText(context, "➔ Записано в A!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("➔ в A", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            modifier = Modifier.weight(1f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple),
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                onTransferToB(lastResultMatrix)
                                Toast.makeText(context, "➔ Записано в B!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("➔ в B", color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        if (onTransferToC != null) {
                            Button(
                                modifier = Modifier.weight(1f).height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber),
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    onTransferToC(lastResultMatrix)
                                    Toast.makeText(context, "➔ Записано в C!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("➔ в C", color = AccentAmber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixResultGridAdaptive(matrix: Array<DoubleArray>, screenWidth: Dp) {
    val rows = matrix.size
    val cols = matrix[0].size
    val colWidth = ((screenWidth - 60.dp) / cols).coerceIn(48.dp, 75.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.height((rows * 28).dp).width(3.dp).background(AccentEmerald, RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp)))
        Spacer(modifier = Modifier.width(6.dp))

        Column {
            for (i in 0 until rows) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (j in 0 until cols) {
                        Box(
                            modifier = Modifier
                                .width(colWidth)
                                .height(28.dp)
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatNumber(matrix[i][j]),
                                color = TextWhite,
                                fontSize = if (colWidth < 55.dp) 12.sp else 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.height((rows * 28).dp).width(3.dp).background(AccentEmerald, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp)))
    }
}

@Composable
fun AdaptiveBtn(text: String, modifier: Modifier = Modifier, highlight: Boolean = false, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "btnScale"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .height(44.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                )
            },
        colors = ButtonDefaults.buttonColors(containerColor = if (highlight) Color(0xFF1E293B) else CardDark),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (highlight) AccentCyan.copy(alpha = 0.6f) else Color(0xFF24334D))
    ) {
        Text(
            text = text,
            color = TextWhite,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Умный парсер скопированного текста в матрицу (понимает табы, пробелы, переводы строк)
// Умный парсер: понимает точки, запятые, любые виды минусов и скобки!
fun parsePastedText(text: String, reqRows: Int, reqCols: Int): List<List<String>>? {
    try {
        // 1. Нормализация текста:
        val normalized = text
            .replace('−', '-') // Типографский минус -> стандартный
            .replace('–', '-') // Короткое тире -> стандартный минус
            .replace('—', '-') // Длинное тире -> стандартный минус
            .replace(',', '.') // ВСЕ ЗАПЯТЫЕ ПРЕВРАЩАЕМ В ТОЧКИ!
            .replace("[", " ") // Убираем скобки если скопировали [1 2; 3 4]
            .replace("]", " ")
            .trim()

        val rawLines = normalized.lines().filter { it.isNotBlank() }
        val allNumbers = mutableListOf<String>()

        // 2. Извлекаем все валидные числа (целые и с плавающей точкой)
        val numberRegex = Regex("-?\\d+(\\.\\d+)?")

        // Проверяем: это многострочный текст или всё в одну строчку
        val result = mutableListOf<List<String>>()

        if (rawLines.size >= reqRows) {
            // Построчный разбор (из таблицы или истории)
            for (i in 0 until reqRows) {
                val rowList = mutableListOf<String>()
                val matches = numberRegex.findAll(rawLines[i]).map { it.value }.toList()
                for (j in 0 until reqCols) {
                    if (j < matches.size) {
                        rowList.add(matches[j])
                    } else {
                        rowList.add("0")
                    }
                }
                result.add(rowList)
            }
        } else {
            // Если скопировали сплошным потоком чисел через пробелы
            val matches = numberRegex.findAll(normalized).map { it.value }.toList()
            var index = 0
            for (i in 0 until reqRows) {
                val rowList = mutableListOf<String>()
                for (j in 0 until reqCols) {
                    if (index < matches.size) {
                        rowList.add(matches[index])
                        index++
                    } else {
                        rowList.add("0")
                    }
                }
                result.add(rowList)
            }
        }

        return result
    } catch (e: Exception) {
        return null
    }
}

fun resizeMatrix(old: List<List<String>>, newR: Int, newC: Int): List<List<String>> {
    return List(newR) { r ->
        List(newC) { c ->
            if (r < old.size && c < old[r].size) old[r][c] else "0"
        }
    }
}

fun parse(data: List<List<String>>): Array<DoubleArray> {
    val r = data.size
    val c = if (r > 0) data[0].size else 0
    return Array(r) { i -> DoubleArray(c) { j -> data[i][j].toDoubleOrNull() ?: 0.0 } }
}

fun matrixToStringList(matrix: Array<DoubleArray>): List<List<String>> {
    return matrix.map { row -> row.map { formatNumber(it) } }
}

fun formatNumber(v: Double): String {
    val clean = if (abs(v) < 1e-9) 0.0 else v
    return if (clean % 1.0 == 0.0) {
        clean.toInt().toString()
    } else {
        String.format(Locale.US, "%.3f", clean).trimEnd('0').trimEnd('.')
    }
}

fun formatMatrixRaw(matrix: Array<DoubleArray>): String {
    return matrix.joinToString("\n") { row ->
        row.joinToString("\t") { v -> formatNumber(v) }
    }
}
// Умный парсер: понимает точки, запятые, любые виды минусов и скобки!
