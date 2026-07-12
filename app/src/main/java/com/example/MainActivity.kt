package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.ShoppingDatabase
import com.example.data.ShoppingRepository
import com.example.ui.ShoppingStopApp
import com.example.ui.ShoppingViewModel
import com.example.ui.ShoppingViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  // === Room 데이터베이스 및 레포지토리 초기화 ===
  private val database by lazy { ShoppingDatabase.getDatabase(this, lifecycleScope) }
  private val repository by lazy { ShoppingRepository(database.shoppingDao()) }

  // === 팩토리를 이용한 뷰모델 주입 ===
  private val viewModel: ShoppingViewModel by viewModels {
    ShoppingViewModelFactory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = false) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          // 쇼핑그만 메인 앱 화면 호출
          ShoppingStopApp(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.stopShoppingAppSession()
  }
}

