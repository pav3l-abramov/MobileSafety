package com.example.inventory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.HashSet

open class NavigationEvent(var isHandled: Boolean = false, var type: Events.Type = Events.Type.EXECUTE_WITHOUT_LIMITS)

class Events private constructor() {
    class EventObserver(private val handlerBlock: (NavigationEvent) -> Unit) :
        Observer<NavigationEvent> {
        private val executedEvents: HashSet<String> = hashSetOf()

        /** Clear Executed Events */
        fun clearExecutedEvents() = executedEvents.clear()

        override fun onChanged(value: NavigationEvent) {
            when (value.type) {
                Type.EXECUTE_WITHOUT_LIMITS,
                Type.WAIT_OBSERVER_IF_NEEDED -> {
                    if (!value.isHandled) {
                        value.isHandled = true
                        value.apply(handlerBlock)
                    }
                }
                Type.EXECUTE_ONCE,
                Type.WAIT_OBSERVER_IF_NEEDED_AND_EXECUTE_ONCE -> {
                    if (value.javaClass.simpleName !in executedEvents) {
                        if (!value.isHandled) {
                            value.isHandled = true
                            executedEvents.add(value.javaClass.simpleName)
                            value.apply(handlerBlock)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    class Emitter : MutableLiveData<NavigationEvent>() {

        /** Default: Emit Event for Execution */
        fun emitAndExecute(event: NavigationEvent) = newEvent(event, Type.EXECUTE_WITHOUT_LIMITS)

        /** Emit Event for Execution Once */
        fun emitAndExecuteOnce(event: NavigationEvent) = newEvent(event, Type.EXECUTE_ONCE)

        /** Wait Observer Available and Emit Event for Execution */
        fun waitAndExecute(event: NavigationEvent) = newEvent(event, Type.WAIT_OBSERVER_IF_NEEDED)

        /** Wait Observer Available and Emit Event for Execution Once */
        fun waitAndExecuteOnce(event: NavigationEvent) = newEvent(event, Type.WAIT_OBSERVER_IF_NEEDED_AND_EXECUTE_ONCE)

        /** Clear Events that are Waiting for Observer */
        fun clearWaitingEvents() = waitingEvents.clear()

        private val waitingEvents: ArrayList<NavigationEvent> = ArrayList()
        private var isActive = false

        override fun onInactive() {
            isActive = false
        }

        override fun onActive() {
            isActive = true
            val postingEvents = ArrayList<NavigationEvent>()
            waitingEvents
                .forEach {
                    if (hasObservers()) {
                        this.value = it
                        postingEvents.add(it)
                    }
                }.also { waitingEvents.removeAll(postingEvents) }
        }

        private fun newEvent(event: NavigationEvent, type: Type) {
            event.type = type
            this.value = when (type) {
                Type.EXECUTE_WITHOUT_LIMITS,
                Type.EXECUTE_ONCE -> if (hasObservers()) event else null

                Type.WAIT_OBSERVER_IF_NEEDED,
                Type.WAIT_OBSERVER_IF_NEEDED_AND_EXECUTE_ONCE -> {
                    if (hasObservers() && isActive) event
                    else {
                        waitingEvents.add(event)
                        null
                    }
                }
            }
        }
    }

    enum class Type {
        EXECUTE_WITHOUT_LIMITS,
        EXECUTE_ONCE,
        WAIT_OBSERVER_IF_NEEDED,
        WAIT_OBSERVER_IF_NEEDED_AND_EXECUTE_ONCE
    }
}
interface MyFragmentNavigation {
    class ShareProduct(val productInfo: String) : NavigationEvent()
}