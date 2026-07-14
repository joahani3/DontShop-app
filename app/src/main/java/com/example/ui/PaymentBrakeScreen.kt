package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BlockedImpulse
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentBrakeScreen(
    viewModel: ShoppingViewModel,
    modifier: Modifier = Modifier
) {
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val blockedImpulses by viewModel.blockedImpulses.collectAsStateWithLifecycle()
    val currentQuote by viewModel.currentQuote.collectAsStateWithLifecycle()
    val resolutionTemplates by remember(isEnglish) {
        derivedStateOf {
            if (isEnglish) viewModel.resolutionTemplatesEn else viewModel.resolutionTemplatesKo
        }
    }

    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var selectedResolution by remember { mutableStateOf(resolutionTemplates.firstOrNull() ?: "") }
    var customResolution by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFEF7F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 헤더
            Text(
                text = if (isEnglish) "Payment Brake" else "결제 브레이크",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 입력 섹션
            PaymentInputSection(
                itemName = itemName,
                onNameChange = { itemName = it },
                itemPrice = itemPrice,
                onPriceChange = { itemPrice = it.filter { c -> c.isDigit() } },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 명언 카드
            QuoteDisplayCard(
                quote = currentQuote,
                onRefresh = { viewModel.rotateQuote() },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 다짐 선택 섹션
            ResolutionSection(
                templates = resolutionTemplates,
                selectedResolution = selectedResolution,
                onSelect = {
                    selectedResolution = it
                    useCustom = false
                },
                customResolution = customResolution,
                onCustomChange = { customResolution = it },
                useCustom = useCustom,
                onUseCustomChange = { useCustom = it },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 액션 버튼
            PaymentBrakeButtons(
                itemName = itemName,
                itemPrice = itemPrice,
                selectedResolution = if (useCustom) customResolution else selectedResolution,
                onSave = {
                    viewModel.triggerBrakeSimulator(itemName, itemPrice)
                },
                onConfirmSave = {
                    viewModel.confirmBrakeAndSave()
                    itemName = ""
                    itemPrice = ""
                    customResolution = ""
                    useCustom = false
                },
                onConfirmPurchase = {
                    viewModel.confirmBrakeAndPurchase()
                    itemName = ""
                    itemPrice = ""
                    customResolution = ""
                    useCustom = false
                },
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 최근 기록
            RecentBrakeRecords(
                records = blockedImpulses.take(5),
                isEnglish = isEnglish
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PaymentInputSection(
    itemName: String,
    onNameChange: (String) -> Unit,
    itemPrice: String,
    onPriceChange: (String) -> Unit,
    isEnglish: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isEnglish) "What do you want to buy?" else "뭘 사고 싶으신가요?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 상품명 입력
            TextField(
                value = itemName,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isEnglish) "Product name" else "상품명",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                },
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                    fontSize = 13.sp
                ),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F7F5),
                    focusedContainerColor = Color(0xFFF8F7F5),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF10B981)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 가격 입력
            TextField(
                value = itemPrice,
                onValueChange = onPriceChange,
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isEnglish) "Price (KRW)" else "가격 (원)",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                    fontSize = 13.sp
                ),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F7F5),
                    focusedContainerColor = Color(0xFFF8F7F5),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF10B981)
                ),
                singleLine = true
            )

            if (itemPrice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₩ ${NumberFormat.getInstance().format(itemPrice.toLongOrNull() ?: 0)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun QuoteDisplayCard(
    quote: String,
    onRefresh: () -> Unit,
    isEnglish: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD1FAE5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = if (isEnglish) "Today's wisdom" else "오늘의 명언",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF10B981),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"$quote\"",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRefresh,
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "✨ ${if (isEnglish) "Refresh" else "갱신"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ResolutionSection(
    templates: List<String>,
    selectedResolution: String,
    onSelect: (String) -> Unit,
    customResolution: String,
    onCustomChange: (String) -> Unit,
    useCustom: Boolean,
    onUseCustomChange: (Boolean) -> Unit,
    isEnglish: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isEnglish) "My Resolution" else "나의 다짐",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 템플릿 선택
            templates.forEach { template ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedResolution == template && !useCustom,
                        onClick = { onSelect(template) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF10B981),
                            unselectedColor = Color(0xFFCBD5E1)
                        )
                    )

                    Text(
                        text = template,
                        fontSize = 12.sp,
                        color = Color(0xFF1F2937),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFFF0EDE9))

            Spacer(modifier = Modifier.height(12.dp))

            // 커스텀 입력
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = useCustom,
                    onClick = { onUseCustomChange(true) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF10B981),
                        unselectedColor = Color(0xFFCBD5E1)
                    )
                )

                Text(
                    text = if (isEnglish) "Custom" else "직접 입력",
                    fontSize = 12.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    fontWeight = FontWeight.Medium
                )
            }

            if (useCustom) {
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = customResolution,
                    onValueChange = onCustomChange,
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = if (isEnglish) "Enter your resolution" else "다짐을 적어주세요",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                        fontSize = 12.sp
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF8F7F5),
                        focusedContainerColor = Color(0xFFF8F7F5),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF10B981)
                    ),
                    minLines = 2
                )
            }
        }
    }
}

@Composable
private fun PaymentBrakeButtons(
    itemName: String,
    itemPrice: String,
    selectedResolution: String,
    onSave: () -> Unit,
    onConfirmSave: () -> Unit,
    onConfirmPurchase: () -> Unit,
    isEnglish: Boolean
) {
    val isValid = itemName.isNotBlank() && itemPrice.isNotEmpty() && selectedResolution.isNotBlank()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onConfirmSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                disabledContainerColor = Color(0xFFCBD5E1)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isEnglish) "✓ Resist & Save" else "✓ 참고 저축하기",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onConfirmPurchase,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF59E0B),
                disabledContainerColor = Color(0xFFCBD5E1)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isEnglish) "✗ Buy Anyway" else "✗ 지르기",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RecentBrakeRecords(
    records: List<BlockedImpulse>,
    isEnglish: Boolean
) {
    if (records.isEmpty()) return

    Text(
        text = if (isEnglish) "Recent Records" else "최근 기록",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF64748B),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            records.forEach { record ->
                BrakeRecordItem(record = record, isEnglish = isEnglish)
                Divider(
                    color = Color(0xFFF0EDE9),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun BrakeRecordItem(
    record: BlockedImpulse,
    isEnglish: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (record.isBlocked) "✓" else "✗",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (record.isBlocked) Color(0xFF10B981) else Color(0xFFF59E0B)
                )

                Text(
                    text = record.itemName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()).format(Date(record.timestamp)),
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Text(
            text = "₩${NumberFormat.getInstance().format(record.itemPrice)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF10B981),
            textAlign = TextAlign.End
        )
    }
}
