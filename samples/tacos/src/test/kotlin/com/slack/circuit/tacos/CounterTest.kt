package com.slack.circuit.tacos

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Event
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.test.presenterTestOf
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Parcelize
object CounterScreen : Screen {
//  sealed interface State : CircuitUiState {
//    object Loading : State
//
//    data class Count(
//      val message: String,
//      val eventSink: (Event) -> Unit
//    ): State
//  }

  data class State(
    val message: String,
    val eventSink: (Click) -> Unit
  ): CircuitUiState

//  sealed interface Event : CircuitUiEvent {
//    data class Increment(val amount: Int = 1) : Event
//    data class Decrement(val amount: Int = 1) : Event
//    object Reset : Event
//  }
  object Click : CircuitUiEvent
}

@Composable
fun counterPresenter(): CounterScreen.State {
  var count by remember { mutableStateOf(0) }
  return CounterScreen.State(
    message = "Count: $count",
    eventSink = { count++ }
  )
}

class CounterPresenter : Presenter<CounterScreen.State> {
  @Composable
  override fun present(): CounterScreen.State {
    var count by remember { mutableStateOf(0) }
    return CounterScreen.State("Count: $count") { _ ->
      count++
    }
  }
}

@Composable
fun CounterUi(
  state: CounterScreen.State,
  modifier: Modifier = Modifier
) {
  val sink = state.eventSink
  Column(modifier = modifier) {
    Text(text = state.message)
    Button(onClick = { sink(CounterScreen.Click) }) {
      Text(text = "Increment Count")
    }
  }
}

@RunWith(RobolectricTestRunner::class)
class CounterFunctionTest {
//  @Test
//  fun `present - emit starting count`() = runTest {
//    moleculeFlow(RecompositionClock.Immediate) {
//      counterPresenter()
//    }.test {
//      val msg = awaitItem().message
//      assertThat(msg).isEqualTo("Count: 0")
//    }
//  }

  @Test
  fun `present - emit starting count`() = runTest {
    presenterTestOf({ counterPresenter() }) {
      val msg = awaitItem().message
      assertThat(msg).isEqualTo("Count: 0")
    }
  }

//  @Test
//  fun `present - click increases count and triggers new state emission`() = runTest {
//    moleculeFlow(RecompositionClock.Immediate) {
//      counterPresenter()
//    }.test {
//      awaitItem().run {
//        assertThat(message).isEqualTo("Count: 0")
//        eventSink(CounterScreen.Click)
//      }
//      assertThat(awaitItem().message).isEqualTo("Count: 1")
//    }
//  }

  @Test
  fun `present - click increases count`() = runTest {
    presenterTestOf({ counterPresenter() }) {
      awaitItem().run {
        assertThat(message).isEqualTo("Count: 0")
        eventSink(CounterScreen.Click)
      }
      assertThat(awaitItem().message).isEqualTo("Count: 1")
    }
  }
}

@RunWith(RobolectricTestRunner::class)
class CounterClassTest {
  @Test
  fun `present - emit starting count`() = runTest {
    CounterPresenter().test {
      val msg = awaitItem().message
      assertThat(msg).isEqualTo("Count: 0")
    }
  }

  @Test
  fun `present - click increases count`() = runTest {
    CounterPresenter().test {
      awaitItem().run {
        assertThat(message).isEqualTo("Count: 0")
        eventSink(CounterScreen.Click)
      }
      assertThat(awaitItem().message).isEqualTo("Count: 1")
    }
  }
}

@RunWith(RobolectricTestRunner::class)
class CounterUiTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun display_count_message() {
    composeTestRule.run {
      setContent {
        CounterUi(
          CounterScreen.State("Count: 5") { /* eventSink */ }
        )
      }

      onNode(hasText("Count: 5")).assertIsDisplayed()
    }
  }

  @Test
  fun emit_event_on_button_click() {
    composeTestRule.run {
      var event by mutableStateOf<CounterScreen.Click?>(null)

      setContent {
        CounterUi(
          CounterScreen.State("Count: 5") { event = it }
        )
      }

      assertThat(event).isNull()
      onNode(hasText("Increment Count")).performClick()
      assertThat(event).isEqualTo(CounterScreen.Click)
    }
  }
}
