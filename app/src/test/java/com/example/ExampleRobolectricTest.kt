package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AuthManager
import com.example.data.CrosshairProfileManager
import com.example.data.SettingsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
  fun `verify app pin 6745 login, fallback, and logout session clearing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authManager = AuthManager(context)

    // Initially logged out
    authManager.logout()
    assertFalse(authManager.isAuthenticated.value)

    // Invalid PIN rejected
    assertFalse(authManager.verifyPin("1234"))
    assertFalse(authManager.verifyPin("0000"))
    assertFalse(authManager.isAuthenticated.value)

    // Official App PIN 6745 accepted
    assertTrue(authManager.verifyPin("6745"))
    assertTrue(authManager.isAuthenticated.value)
    assertEquals("PIN", authManager.lastAuthMethod.value)

    // Logout clears session completely
    authManager.logout()
    assertFalse(authManager.isAuthenticated.value)
    assertEquals("", authManager.lastAuthMethod.value)
  }

  @Test
  fun `verify rate limiting lockout after 5 failed attempts`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authManager = AuthManager(context)

    authManager.logout()

    // 4 failed attempts should not trigger full lockout yet
    for (i in 1..4) {
      assertFalse(authManager.verifyPin("999$i"))
    }
    assertFalse(authManager.isLockedOut())

    // 5th failed attempt triggers lockout
    assertFalse(authManager.verifyPin("9995"))
    assertTrue(authManager.isLockedOut())
    assertTrue(authManager.getRemainingLockoutSeconds() > 0)

    // Even correct PIN is rejected while locked out
    assertFalse(authManager.verifyPin("6745"))
    assertFalse(authManager.isAuthenticated.value)

    // Reset session
    authManager.logout()
  }

  @Test
  fun `verify session persistence across app restart`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Login with first AuthManager instance
    val authManager1 = AuthManager(context)
    authManager1.logout()
    assertTrue(authManager1.verifyPin("6745"))
    assertTrue(authManager1.isAuthenticated.value)

    // Simulate app restart by creating a new AuthManager instance
    val authManager2 = AuthManager(context)
    assertTrue(authManager2.isAuthenticated.value)

    // Logout from second instance
    authManager2.logout()
    assertFalse(authManager2.isAuthenticated.value)

    // Verify third instance also sees logged out state
    val authManager3 = AuthManager(context)
    assertFalse(authManager3.isAuthenticated.value)
  }

  @Test
  fun `verify biometric status check and safe fallback to PIN`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authManager = AuthManager(context)

    // On standard test runner without biometric hardware enrolled,
    // isBiometricAvailable() safely returns false without crashing
    val isBioAvailable = authManager.isBiometricAvailable()
    val status = authManager.checkBiometricStatus()
    assertNotNull(status)

    // In non-biometric environments, PIN fallback works seamlessly
    authManager.logout()
    assertFalse(authManager.verifyPin("wrong_pin"))
    assertTrue(authManager.verifyPin("6745"))
    assertTrue(authManager.isAuthenticated.value)

    authManager.logout()
  }

  @Test
  fun `verify crosshair profile manager persistence and real-time updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val settingsManager = SettingsManager(context)
    val profileManager = CrosshairProfileManager(context, settingsManager)

    // Modify settings
    settingsManager.setCrosshairStyle("DIAMOND")
    settingsManager.setCrosshairSize(42)
    settingsManager.setCrosshairThickness(3.5f)
    settingsManager.setCrosshairOffset(10, -15)

    // Test creating custom profile
    profileManager.createNewProfile("Test Custom Diamond")
    val profiles = profileManager.profiles.value
    assertTrue(profiles.any { it.name == "Test Custom Diamond" })

    val created = profiles.find { it.name == "Test Custom Diamond" }
    assertNotNull(created)
    assertEquals(42, created?.size)
    assertEquals("DIAMOND", created?.style)
    assertEquals(10, created?.offsetX)
    assertEquals(-15, created?.offsetY)

    // Select default profile and verify values updated in settingsManager
    profileManager.selectProfile("default")
    assertEquals("CLASSIC_CROSS", settingsManager.crosshairStyle.value)
    assertEquals(28, settingsManager.crosshairSize.value)
  }

  @Test
  fun `verify reset position and reset style independently`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val settingsManager = SettingsManager(context)

    // Set custom values
    settingsManager.setCrosshairOffset(25, -30)
    settingsManager.setCrosshairAutoCenter(false)
    settingsManager.setCrosshairSize(50)
    settingsManager.setCrosshairStyle("SNIPER")

    // Reset Position only
    settingsManager.resetCrosshairPosition()
    assertEquals(0, settingsManager.crosshairOffsetX.value)
    assertEquals(0, settingsManager.crosshairOffsetY.value)
    assertTrue(settingsManager.crosshairAutoCenter.value)
    // Style remains custom
    assertEquals(50, settingsManager.crosshairSize.value)
    assertEquals("SNIPER", settingsManager.crosshairStyle.value)

    // Reset Style only
    settingsManager.resetCrosshairStyle()
    assertEquals(28, settingsManager.crosshairSize.value)
    assertEquals("CLASSIC_CROSS", settingsManager.crosshairStyle.value)
  }

  @Test
  fun `verify legacy keys case sensitively`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authManager = AuthManager(context)

    authManager.logout()
    assertFalse(authManager.isAuthenticated.value)

    // Invalid key rejected
    assertFalse(authManager.verifyKey("invalid_key"))
    assertFalse(authManager.verifyKey("dkvx59hh")) // wrong case
    assertFalse(authManager.isAuthenticated.value)

    // Valid key 1 accepted
    assertTrue(authManager.verifyKey("DKVX59HH"))
    assertTrue(authManager.isAuthenticated.value)

    // Logout
    authManager.logout()
    assertFalse(authManager.isAuthenticated.value)

    // Valid key 2 accepted
    assertTrue(authManager.verifyKey("ZXKUTS5"))
    assertTrue(authManager.isAuthenticated.value)

    authManager.logout()
  }
}
