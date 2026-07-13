package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BlockedImpulse
import com.example.data.ShoppingApp
import com.example.data.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import kotlin.random.Random
import java.util.Locale

class ShoppingViewModel(private val repository: ShoppingRepository) : ViewModel() {

    // === 상태 관찰 ===
    val shoppingApps: StateFlow<List<ShoppingApp>> = repository.allShoppingApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val blockedImpulses: StateFlow<List<BlockedImpulse>> = repository.allBlockedImpulses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override fun onCleared() {
        super.onCleared()
        runCatching {
            tickingJob?.cancel()
            tickingJob = null
        }
    }

    // === UI 네비게이션 탭 상태 ===
    // 0: 대시보드 (통계 + 오늘의 명언)
    // 1: 쇼핑앱 제어 (앱 목록 추가, 사용량 조정, 잠금 설정)
    // 2: 결제 브레이크 (시뮬레이터, 충동구매 제어 테스트 및 다짐 모음)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    // === 명언 목록 ===
    val wiseQuotesKo = listOf(
        "오늘 산 물건은 내일 쓰레기가 되고, 오늘 남긴 돈은 내일 기회가 된다.",
        "가장 뛰어난 가성비 할인은 100% 할인율인 '안 사는 것'이다.",
        "이번 달의 나를 희생하여 다음 달의 나에게 빚을 넘기지 말라.",
        "지름신이 올 때는 내 가슴에 손을 얹고 딱 3초만 물어보자. '진짜 필요해?'",
        "물건을 소유하는 행복보다 내 통장 잔고가 온전히 지켜지는 안도감이 더 크다.",
        "충동적으로 산 물건은 옷장이 기억하고, 낭비된 돈은 당신의 미래가 기억한다.",
        "카드를 긁는 순간의 쾌락은 3초지만, 카드 청구서를 마주하는 고통은 30일이다.",
        "할인한다고 필요 없는 물건을 사는 것은 돈을 절약한 게 아니라 버린 것이다.",
        "당신의 지쳐있는 영혼을 채우기 위해 가짜 만족감(택배 상자)을 소비하지 마세요.",
        "소비는 잠시 짜릿하지만, 저축은 언제나 든든하다. 쇼핑을 그만두면 미래가 바뀐다."
    )

    val wiseQuotesEn = listOf(
        "What you buy today becomes trash tomorrow; what you save today becomes an opportunity tomorrow.",
        "The best discount is a 100% discount, which is 'not buying it'.",
        "Do not sacrifice your present self to pass debt to your future self next month.",
        "When the urge to buy hits, put your hand on your heart and ask yourself for 3 seconds: 'Do I really need this?'",
        "The security of keeping your bank account intact is far greater than the joy of possessing things.",
        "Your closet remembers items bought impulsively; your future remembers wasted money.",
        "The pleasure of swiping a card is 3 seconds, but the pain of facing the bill is 30 days.",
        "Buying unnecessary items just because they are on sale is throwing money away, not saving it.",
        "Do not consume fake satisfaction (delivery boxes) to fill your exhausted soul.",
        "Spending is temporarily thrilling, but saving is always reassuring. Stop shopping and your future will change."
    )

    val wiseQuotes: List<String>
        get() = if (_isEnglish.value) wiseQuotesEn else wiseQuotesKo

    private val _currentQuote = MutableStateFlow(
        if (Locale.getDefault().language != "ko") wiseQuotesEn[0] else wiseQuotesKo[0]
    )
    val currentQuote: StateFlow<String> = _currentQuote.asStateFlow()

    fun rotateQuote() {
        val quotes = wiseQuotes
        val nextIndex = Random.nextInt(quotes.size)
        _currentQuote.value = quotes[nextIndex]
    }

    // === 나의 다짐 5가지 템플릿 ===
    val resolutionTemplatesKo = listOf(
        "진짜 필요한 것인지 최소 24시간 뒤에 다시 생각하자.",
        "이번 달 예산을 초과하면 다음 달의 내가 피눈물 흘리며 고통받는다.",
        "장바구니에만 담아두고 일주일 뒤에 냉정하게 다시 열어보자.",
        "물건을 사서 느껴지는 도파민 행복은 단 3일뿐이다.",
        "내 꿈과 든든한 미래를 위해 이 돈을 저축하거나 우량주에 투자하자."
    )

    val resolutionTemplatesEn = listOf(
        "Think again for at least 24 hours to see if it is truly necessary.",
        "If I exceed my budget this month, next month's me will suffer in pain.",
        "Leave it in the cart and review it coldly a week later.",
        "The dopamine happiness of buying an item lasts only 3 days.",
        "Let's save this money or invest in blue-chip stocks for my dreams and a secure future."
    )

    val resolutionTemplates: List<String>
        get() = if (_isEnglish.value) resolutionTemplatesEn else resolutionTemplatesKo

    // === 결제 브레이크 팝업창(다시 생각하기 시뮬레이션) 상태 ===
    private val _showBrakeDialog = MutableStateFlow(false)
    val showBrakeDialog: StateFlow<Boolean> = _showBrakeDialog.asStateFlow()

    private val _brakeItemName = MutableStateFlow("")
    val brakeItemName: StateFlow<String> = _brakeItemName.asStateFlow()

    private val _brakeItemPrice = MutableStateFlow("")
    val brakeItemPrice: StateFlow<String> = _brakeItemPrice.asStateFlow()

    private val _selectedResolution = MutableStateFlow(
        if (Locale.getDefault().language != "ko") resolutionTemplatesEn[0] else resolutionTemplatesKo[0]
    )
    val selectedResolution: StateFlow<String> = _selectedResolution.asStateFlow()

    private val _userResolutionInput = MutableStateFlow("")
    val userResolutionInput: StateFlow<String> = _userResolutionInput.asStateFlow()

    // 팝업 결과 성공/실패 다이얼로그
    private val _showResultDialog = MutableStateFlow<Boolean?>(null) // null, true(참음), false(질러버림)
    val showResultDialog: StateFlow<Boolean?> = _showResultDialog.asStateFlow()

    // 경고 메시지 토스트/배너 상태
    private val _warningMessage = MutableStateFlow<String?>(null)
    val warningMessage: StateFlow<String?> = _warningMessage.asStateFlow()

    // === 실시간 쇼핑 세션 제어 ===
    private var tickingJob: kotlinx.coroutines.Job? = null
    private val sessionMutex = Mutex()

    private val _activeApp = MutableStateFlow<ShoppingApp?>(null)
    val activeApp: StateFlow<ShoppingApp?> = _activeApp.asStateFlow()

    fun startShoppingAppSession(app: ShoppingApp) {
        if (app.isLocked || app.usedMinutesToday >= app.dailyLimitMinutes) {
            _warningMessage.value = "⚠️ [잠금 상태] ${app.name}은(는) 오늘 사용 제한 시간을 초과했거나 잠겨 있습니다!"
            return
        }

        viewModelScope.launch {
            sessionMutex.withLock {
                _activeApp.value = app
                tickingJob?.cancel()
                tickingJob = viewModelScope.launch {
                    try {
                        val dailyLimit = app.dailyLimitMinutes

                        while (this.isActive) {
                            delay(2000)

                            val currentActive = _activeApp.value ?: break
                            if (currentActive.id != app.id) break

                            val newMinutes = currentActive.usedMinutesToday + 1
                            repository.updateUsedMinutes(currentActive.id, newMinutes)

                            val updated = currentActive.copy(usedMinutesToday = newMinutes)
                            _activeApp.value = updated
                            checkAppLimits(updated)

                            if (newMinutes >= dailyLimit) {
                                repository.updateAppLockState(currentActive.id, true)
                                _activeApp.value = null
                                _warningMessage.value = "🚨 [시간 초과 잠금] ${currentActive.name}의 사용 한도가 다 되어서 세션이 자동으로 차단되었습니다!"
                                break
                            }
                        }
                    } catch (e: Exception) {
                        _warningMessage.value = "세션 오류: ${e.message}"
                        _activeApp.value = null
                    }
                }
            }
        }
    }

    fun addSessionMinuteSimulated() {
        val currentActive = _activeApp.value ?: return
        val dailyLimit = currentActive.dailyLimitMinutes
        val appName = currentActive.name
        val appId = currentActive.id

        viewModelScope.launch {
            try {
                val newMinutes = currentActive.usedMinutesToday + 1
                repository.updateUsedMinutes(appId, newMinutes)
                val updated = currentActive.copy(usedMinutesToday = newMinutes)
                _activeApp.value = updated
                checkAppLimits(updated)

                if (newMinutes >= dailyLimit) {
                    repository.updateAppLockState(appId, true)
                    _activeApp.value = null
                    _warningMessage.value = "🚨 [시간 초과 잠금] ${appName}의 사용 한도가 다 되어서 세션이 자동으로 차단되었습니다!"
                }
            } catch (e: Exception) {
                _warningMessage.value = "세션 분 추가 실패: ${e.message}"
            }
        }
    }

    fun stopShoppingAppSession() {
        viewModelScope.launch {
            sessionMutex.withLock {
                _activeApp.value = null
                tickingJob?.cancel()
                tickingJob = null
            }
        }
    }

    // === 사용자 설정 상태 (이미지 3 대응) ===
    private val _monthlyBudget = MutableStateFlow(400000L)
    val monthlyBudget: StateFlow<Long> = _monthlyBudget.asStateFlow()

    private val _overspendingAlert = MutableStateFlow("예산 초과 시")
    val overspendingAlert: StateFlow<String> = _overspendingAlert.asStateFlow()

    private val _shoppingTimeLimit = MutableStateFlow("1일 1시간")
    val shoppingTimeLimit: StateFlow<String> = _shoppingTimeLimit.asStateFlow()

    private val _nightAlertLimit = MutableStateFlow(true)
    val nightAlertLimit: StateFlow<Boolean> = _nightAlertLimit.asStateFlow()

    private val _isEnglish = MutableStateFlow(Locale.getDefault().language != "ko")
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    fun setMonthlyBudget(budget: Long) {
        _monthlyBudget.value = budget
    }

    fun setOverspendingAlert(option: String) {
        _overspendingAlert.value = option
    }

    fun setShoppingTimeLimit(option: String) {
        _shoppingTimeLimit.value = option
    }

    fun toggleNightAlertLimit() {
        _nightAlertLimit.value = !_nightAlertLimit.value
    }

    fun clearWarning() {
        _warningMessage.value = null
    }

    // === 비즈니스 액션 ===

    // 쇼핑앱 추가
    fun addShoppingApp(name: String, limitMinutes: Int, category: String, packageName: String? = null) {
        if (name.isBlank()) {
            _warningMessage.value = "앱 이름을 입력해주세요!"
            return
        }
        if (limitMinutes <= 0) {
            _warningMessage.value = "제한 시간은 0보다 커야 합니다!"
            return
        }

        viewModelScope.launch {
            try {
                val app = ShoppingApp(
                    name = name,
                    dailyLimitMinutes = limitMinutes,
                    usedMinutesToday = 0,
                    isLocked = false,
                    packageName = packageName ?: "com.custom.${System.currentTimeMillis()}",
                    category = category
                )
                repository.insertShoppingApp(app)
            } catch (e: Exception) {
                _warningMessage.value = "앱 추가 실패: ${e.message}"
            }
        }
    }

    // 다중 쇼핑앱 추가
    fun addShoppingApps(appsList: List<Triple<String, String, String?>>, limitMinutes: Int) {
        if (appsList.isEmpty()) {
            _warningMessage.value = "추가할 앱이 없습니다!"
            return
        }
        if (limitMinutes <= 0) {
            _warningMessage.value = "제한 시간은 0보다 커야 합니다!"
            return
        }

        viewModelScope.launch {
            try {
                appsList.forEachIndexed { index, (name, category, packageName) ->
                    if (name.isNotBlank()) {
                        val app = ShoppingApp(
                            name = name,
                            dailyLimitMinutes = limitMinutes,
                            usedMinutesToday = 0,
                            isLocked = false,
                            packageName = packageName ?: "com.custom.${System.currentTimeMillis()}_${index}",
                            category = category
                        )
                        repository.insertShoppingApp(app)
                    }
                }
                _warningMessage.value = "앱 ${appsList.size}개 추가 완료!"
            } catch (e: Exception) {
                _warningMessage.value = "앱 추가 중 오류: ${e.message}"
            }
        }
    }

    // 쇼핑앱 정보 수정 (한도 시간 등)
    fun updateShoppingApp(app: ShoppingApp) {
        viewModelScope.launch {
            repository.updateShoppingApp(app)
            checkAppLimits(app)
        }
    }

    // 사용 시간 변경 (사용자 시뮬레이션)
    fun simulateAppUsage(app: ShoppingApp, deltaMinutes: Int) {
        if (deltaMinutes < 0 && app.usedMinutesToday + deltaMinutes < 0) {
            _warningMessage.value = "사용 시간이 0보다 작을 수 없습니다!"
            return
        }

        viewModelScope.launch {
            try {
                val newMinutes = (app.usedMinutesToday + deltaMinutes).coerceAtLeast(0)
                repository.updateUsedMinutes(app.id, newMinutes)

                val updatedApp = app.copy(usedMinutesToday = newMinutes)
                checkAppLimits(updatedApp)
            } catch (e: Exception) {
                _warningMessage.value = "사용 시간 변경 실패: ${e.message}"
            }
        }
    }

    private fun checkAppLimits(app: ShoppingApp) {
        if (app.usedMinutesToday >= app.dailyLimitMinutes) {
            _warningMessage.value = "⚠️ [경고] ${app.name} 사용 시간이 오늘 제한량(${app.dailyLimitMinutes}분)을 초과했습니다! 지금 즉시 쇼핑을 중단하세요!"
        } else if (app.usedMinutesToday >= app.dailyLimitMinutes * 0.8) {
            _warningMessage.value = "🔔 [알림] ${app.name} 사용 시간이 80%를 초과했습니다. 조금만 더 참으세요!"
        }
    }

    // 잠금 상태 토글
    fun toggleAppLock(app: ShoppingApp) {
        viewModelScope.launch {
            repository.updateAppLockState(app.id, !app.isLocked)
        }
    }

    // 쇼핑앱 삭제
    fun deleteShoppingApp(app: ShoppingApp) {
        viewModelScope.launch {
            repository.deleteShoppingApp(app)
        }
    }

    // 결제 브레이크 팝업 호출 (시뮬레이터 실행)
    fun triggerBrakeSimulator(itemName: String, itemPriceString: String) {
        val price = itemPriceString.filter { it.isDigit() }.toIntOrNull() ?: 0
        if (itemName.isBlank()) {
            _warningMessage.value = "살 물건의 이름을 적어주세요!"
            return
        }
        if (price <= 0) {
            _warningMessage.value = "올바른 가격을 입력해주세요!"
            return
        }
        
        _brakeItemName.value = itemName
        _brakeItemPrice.value = price.toString()
        _selectedResolution.value = resolutionTemplates[0]
        _userResolutionInput.value = ""
        _showBrakeDialog.value = true
        rotateQuote() // 팝업 띄울 때 신선한 명언 갱신
    }

    fun selectResolution(res: String) {
        _selectedResolution.value = res
        _userResolutionInput.value = "" // 다짐 변경 시 입력값 비우기
    }

    fun setUserResolutionInput(text: String) {
        _userResolutionInput.value = text
    }

    // 충동구매 참기 완료 (참기 성공!)
    fun confirmBrakeAndSave() {
        val name = _brakeItemName.value
        val priceStr = _brakeItemPrice.value
        val price = priceStr.filter { it.isDigit() }.toIntOrNull() ?: 0
        val resolution = if (_userResolutionInput.value.isNotBlank()) _userResolutionInput.value else _selectedResolution.value

        if (name.isBlank()) {
            _warningMessage.value = "상품명을 입력해주세요!"
            return
        }
        if (price <= 0) {
            _warningMessage.value = "올바른 가격을 입력해주세요! (0보다 커야 함)"
            return
        }
        if (resolution.isBlank()) {
            _warningMessage.value = "다짐을 선택해주세요!"
            return
        }

        viewModelScope.launch {
            try {
                val record = BlockedImpulse(
                    itemName = name,
                    itemPrice = price,
                    resolutionSelected = resolution,
                    isBlocked = true
                )
                repository.insertBlockedImpulse(record)
                _showBrakeDialog.value = false
                _showResultDialog.value = true
            } catch (e: Exception) {
                _warningMessage.value = "기록 저장 실패: ${e.message}"
                _showBrakeDialog.value = false
            }
        }
    }

    // 결국 참지 못하고 결제 (지름 강행)
    fun confirmBrakeAndPurchase() {
        val name = _brakeItemName.value
        val priceStr = _brakeItemPrice.value
        val price = priceStr.filter { it.isDigit() }.toIntOrNull() ?: 0
        val userInput = _userResolutionInput.value
        val resolution = "다짐을 지키지 못함: " + (if (userInput.isNotBlank()) userInput else _selectedResolution.value)

        if (name.isBlank()) {
            _warningMessage.value = "상품명을 입력해주세요!"
            return
        }
        if (price <= 0) {
            _warningMessage.value = "올바른 가격을 입력해주세요! (0보다 커야 함)"
            return
        }

        viewModelScope.launch {
            try {
                val record = BlockedImpulse(
                    itemName = name,
                    itemPrice = price,
                    resolutionSelected = resolution,
                    isBlocked = false
                )
                repository.insertBlockedImpulse(record)
                _showBrakeDialog.value = false
                _showResultDialog.value = false
            } catch (e: Exception) {
                _warningMessage.value = "기록 저장 실패: ${e.message}"
                _showBrakeDialog.value = false
            }
        }
    }

    fun closeResultDialog() {
        _showResultDialog.value = null
    }

    fun closeBrakeDialog() {
        _showBrakeDialog.value = false
    }

    // 전체 기록 초기화 (통계 리셋)
    fun resetStatistics() {
        viewModelScope.launch {
            repository.clearAllBlockedImpulses()
        }
    }
}

class ShoppingViewModelFactory(private val repository: ShoppingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
