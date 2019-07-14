package com.telco.services


import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class MSISDNOf(val serviceProvider: Party, val subscriber: Party, val customerID: String)