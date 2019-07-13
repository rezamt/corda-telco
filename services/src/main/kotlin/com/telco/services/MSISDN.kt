package com.telco.services

import net.corda.core.contracts.CommandData
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class MSISDN(val of: MSISDNOf, val value: Boolean) : CommandData
