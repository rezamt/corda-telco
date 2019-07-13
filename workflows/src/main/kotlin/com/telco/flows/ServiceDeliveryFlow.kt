package com.telco.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class ServiceDeliveryFlow : FlowLogic<Unit>() {

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
