package com.telco.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

import java.time.LocalDate

import com.telco.states.InvoiceState
/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [InvoiceState], which in turn encapsulates an [InvoiceState].
 *
 * For a new [InvoiceState] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [InvoiceState].
 * - An Create() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class BillingContract : Contract {

    companion object {
        @JvmStatic
        val ID = "com.telco.contracts.BillingContract"
    }

    /*
        var serviceType: String, var issueDate: LocalDate, var dueDate: LocalDate, var amount: Double, val status: String,

     */


    // Commands signed by oracles must contain the facts the oracle is attesting to.
    interface Commands : CommandData {
        class IssueBill(val subscriber: Party, val serviceProvider: Party, val customerID: String, var billingAccountID: String, var serviceType: String, var issueDate: LocalDate, var dueDate: LocalDate, var amount: Double, val status: String) : Commands
        class PayBill(val subscriber: Party, val serviceProvider: Party, val customerID: String, var billingAccountID: String, var serviceType: String,  var dueDate: LocalDate, val amount: Double) : Commands
        class OverDueBill(val subscriber: Party, val serviceProvider: Party, val customerID: String, var billingAccountID: String, var serviceType: String,  var dueDate: LocalDate, val amount: Double) : Commands
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.IssueBill -> {
                requireThat {
                }
            }
            is Commands.PayBill -> {
                requireThat {
                }
            }
            is Commands.OverDueBill -> {
                requireThat {
                }
            }
        }
    }
}
