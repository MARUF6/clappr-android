package io.clappr.player.base

import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
open class UIObjectCreationTest {

    @Test
    fun uiObjectCreation() {
        BaseObject.applicationContext = ApplicationProvider.getApplicationContext()
        val uo = UIObject()
        assertNotNull(uo.view, "invalid view")
    }
}