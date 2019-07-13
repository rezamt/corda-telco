package com.telco.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy


@InitiatedBy(ServiceChargingFlow::class)
class ServiceChargingFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}