package com.telco.contracts

import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

import net.corda.core.contracts.*

@LegalProseReference("https://nowhere/nolegal")
class SubscriptionContract : Contract {


    companion object {
        @JvmStatic
        val ID = "com.telco.contracts.SubscriptionContract"
    }

    // Commands signed by oracles must contain the facts the oracle is attesting to.
    interface Commands : CommandData {
        class Subscribe(val subscriber: Party, val serviceProvider: Party) : Commands
        class UnSubscribe(val subscriber: Party, val serviceProvider: Party) : Commands
        class Approved(val subscriber: Party, val serviceProvider: Party, val accountStatus: String, val serviceStatus: String) : Commands
        class Connected(val subscriber: Party, val serviceProvider: Party, val accountStatus: String, val serviceStatus: String) : Commands
        class Activated(val subscriber: Party, val serviceProvider: Party, val accountStatus: String, val serviceStatus: String) : Commands
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

            is Commands.Approved -> {
                requireThat {
                }
            }

        }
    }

}