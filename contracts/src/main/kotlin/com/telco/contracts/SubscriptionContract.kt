package com.telco.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

import java.time.LocalDate

import com.telco.states.InvoiceState

class SubscriptionContract : Contract {


    companion object {
        @JvmStatic
        val ID = "com.telco.contracts.SubscriptionContract"
    }

    // Commands signed by oracles must contain the facts the oracle is attesting to.
    interface Commands : CommandData {
        class Subscribe(val customer: Party, val serviceProvider: Party) : Commands
        class UnSubscribe(val customer: Party, val serviceProvider: Party) : Commands
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Subscribe -> {
                requireThat {
                }
            }
            is Commands.UnSubscribe -> {
                requireThat {
                }
            }

        }
    }

}