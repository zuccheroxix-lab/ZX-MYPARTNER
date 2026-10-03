package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DYNIMETIZE ZX", appName)
  }

  @Test
  fun `verify valid keys case sensitively and session persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authManager = com.example.data.AuthManager(context)

    // Initially not logged in
    assertEquals(false, authManager.isAuthenticated.value)

    // Invalid key rejected
    assertEquals(false, authManager.verifyKey("invalid_key"))
    assertEquals(false, authManager.verifyKey("dkvx59hh")) // wrong case
    assertEquals(false, authManager.isAuthenticated.value)

    // Valid key 1 accepted
    assertEquals(true, authManager.verifyKey("DKVX59HH"))
    assertEquals(true, authManager.isAuthenticated.value)

    // Logout
    authManager.logout()
    assertEquals(false, authManager.isAuthenticated.value)

    // Valid key 2 accepted
    assertEquals(true, authManager.verifyKey("ZXKUTS5"))
    assertEquals(true, authManager.isAuthenticated.value)
  }
}
