package io.clappr.player.base

import android.os.Bundle
import io.clappr.player.BuildConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23), shadows = [ShadowLog::class])
open class BaseObjectTest {
    var baseObject: BaseObject? = null
    var callbackWasCalled = false

    private val eventName = "some-event"
    val callback: EventHandler = { callbackWasCalled = true }

    @Before
    fun setup() {
        BaseObject.applicationContext = ShadowApplication.getInstance().applicationContext
        baseObject = BaseObject()
        callbackWasCalled = false
    }

    @Test
    fun onCallbackShouldBeCalledOnEventTrigger() {
        baseObject?.on(eventName, callback)
        baseObject?.trigger(eventName)

        assertTrue("event not triggered", callbackWasCalled)
    }

    @Test
    fun onCallbackShouldReceiveUserInfo() {
        var value = "Not Expected"

        baseObject?.on(eventName) { bundle -> value = bundle?.getString("value")!! }
        val userData = Bundle()
        userData.putString("value", "Expected")
        baseObject?.trigger(eventName, userData)

        assertTrue("userInfo not received", value == "Expected")
    }

    @Test
    fun onCallbackShouldBeCalledForEveryCallback() {
        baseObject?.on(eventName, callback)

        var secondCallbackCalled = false
        baseObject?.on(eventName) { secondCallbackCalled = true }

        baseObject?.trigger(eventName)

        assertTrue("event not triggered", callbackWasCalled)
        assertTrue("second event not triggered", secondCallbackCalled)
    }

    @Test
    fun onCallbackShouldOnlyBeRegisteredOnce() {
        var numberOfCalls = 0
        val localCallback: EventHandler = { numberOfCalls += 1 }
        baseObject?.on(eventName, localCallback)
        baseObject?.on(eventName, localCallback)

        baseObject?.trigger(eventName)

        assertEquals("event handler should only be called once", numberOfCalls, 1)
    }

    @Test
    fun onCallbackShouldNotBeCalledforAnotherTrigger() {
        baseObject?.on(eventName, callback)

        baseObject?.trigger("another-event")

        assertFalse("event triggered", callbackWasCalled)
    }

    @Test
    fun onCallbackShouldNotBeCalledforAnotherObject() {
        val anotherObject = BaseObject()

        baseObject?.on(eventName, callback)

        anotherObject.trigger(eventName)

        assertFalse("event triggered", callbackWasCalled)
    }

    @Test
    fun onceCallbackShouldBeCalledOnEvent() {
        baseObject?.once(eventName, callback)
        baseObject?.trigger(eventName)

        assertTrue("event not triggered", callbackWasCalled)
    }

    @Test
    fun onceCallbackShouldNotBeCalledTwice() {
        baseObject?.once(eventName, callback)

        baseObject?.trigger(eventName)
        callbackWasCalled = false
        baseObject?.trigger(eventName)

        assertFalse("event triggered", callbackWasCalled)
    }

    @Test
    fun onceCallbackShouldNotBeCalledIfRemoved() {
        val listenId = baseObject?.once(eventName, callback)
        baseObject?.off(listenId!!)
        baseObject?.trigger(eventName)

        assertFalse("event triggered", callbackWasCalled)
    }

    @Test
    fun listenToShouldFireAnEvent() {
        val contextObject = BaseObject()

        baseObject?.listenTo(contextObject, eventName, callback)
        contextObject.trigger(eventName)

        assertTrue("event not triggered", callbackWasCalled)
    }

    @Test
    fun listenToShouldHandleCallbackException() {
        val brokenCallback: EventHandler = { throw NullPointerException() }
        val expectedLogMessage = "[BaseObject] Plugin ${brokenCallback.javaClass.name} " +
                "crashed during invocation of event $eventName"

        val contextObject = BaseObject()

        baseObject?.listenTo(contextObject, eventName, brokenCallback)
        contextObject.trigger(eventName)

        assertEquals(expectedLogMessage, ShadowLog.getLogs()[0].msg)
    }

    @Test
    fun offCallbackNotCalledIfRemoved() {
        val listenId = baseObject?.on(eventName, callback)
        baseObject?.off(listenId!!)
        baseObject?.trigger(eventName)

        assertFalse("event triggered", callbackWasCalled)
    }

    @Test
    fun offOtherShouldBeCalledAfterRemoval() {
        var anotherCallbackWasCalled = false
        val anotherCallback: EventHandler = { anotherCallbackWasCalled = true}

        val listenId = baseObject?.on(eventName, callback)
        baseObject?.on(eventName, anotherCallback)

        baseObject?.off(listenId!!)
        baseObject?.trigger(eventName)

        assertFalse("event triggered", callbackWasCalled)
        assertTrue("event not triggered", anotherCallbackWasCalled)
    }

    @Test
    fun stopListeningShouldCancelAllHandlers() {
        baseObject?.on(eventName, callback)
        baseObject?.on("another-event", callback)

        baseObject?.stopListening()

        baseObject?.trigger(eventName)
        baseObject?.trigger("another-event")

        assertFalse("event triggered", callbackWasCalled)
    }

    @Test
    fun stopListeningShouldCancelOnlyOnObject() {
        val anotherObject = BaseObject()
        var anotherCallbackWasCalled = false
        anotherObject.on(eventName, { anotherCallbackWasCalled = true})

        baseObject?.on(eventName, callback)

        baseObject?.stopListening()

        baseObject?.trigger(eventName)
        anotherObject.trigger(eventName)

        assertFalse("event triggered", callbackWasCalled)
        assertTrue("event not triggered", anotherCallbackWasCalled)
    }

    @Test
    fun stopListeningShouldCancelOnBaseObject() {
        val contextObject = BaseObject()

        val listenId = baseObject?.listenTo(contextObject, eventName, callback)
        baseObject?.stopListening(listenId!!)

        contextObject.trigger(eventName)

        assertFalse("event triggered", callbackWasCalled)
    }
}