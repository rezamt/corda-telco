package com.telco.flows

import co.paralleluniverse.fibers.Suspendable
import com.telco.contracts.SubscriptionContract
import com.telco.states.SubscriptionState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * This flow allows two parties (the [Initiator] to come to an agreement about the Subscription encapsulated
 * within an [SubscriptionState].
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 *
 * Sample call:
 *   start IssueInvoiceFlow hoursWorked: 8, date: 2019-05-20, otherParty: "O=MegaCorp 1,L=New York,C=US"
 */
@InitiatingFlow
@StartableByRPC
class SubscriptionFlow(
        private val customerID: String,
        private val firstName: String,
        private val lastName: String,
        private val email: String,
        private val serviceType: String,
        private val serviceLevel: String,
        private val contractID: String,
        private val billingAccountID: String,
//        private val subscriptionStartDate: LocalDate,
//        private val subscriptionEndDate: LocalDate,
        private val serviceProvider: Party // IRCell, TurkCell = CName
) : FlowLogic<SignedTransaction>() {

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object START_SUBSCRIPTION : ProgressTracker.Step("Start Subscription Flow.")
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on Service provider, Service Type, Service Level submission.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object ORACLE_SIGNS : ProgressTracker.Step("Requesting oracle signature.")
        object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                START_SUBSCRIPTION,
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                // ORACLE_SIGNS,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    override fun call(): SignedTransaction {
        // Obtain a reference to the notary we want to use.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        progressTracker.currentStep = START_SUBSCRIPTION

        // "Thu Jul 11 2019"
        val subscriptionStartDate = LocalDate.parse("Thu Jul 4 2019", DateTimeFormatter.ofPattern("EEE MMM d yyyy"))
        val subscriptionEndDate = LocalDate.parse("Thu Jul 4 2019", DateTimeFormatter.ofPattern("EEE MMM d yyyy"))


        // Stage 1.
        // Access Oracle to get Information

        // Stage 2.
        progressTracker.currentStep = GENERATING_TRANSACTION

        // Generate an unsigned transaction.
        val subscriptionState = SubscriptionState(
                customerID,
                firstName,
                lastName,
                email,
                serviceType,
                serviceLevel,
                contractID,
                billingAccountID,
                subscriptionStartDate,
                subscriptionEndDate,
                serviceProvider,
                ourIdentity
        );


        // val commandSigners = subscriptionState.participants.plus(serviceProvider).map { it.owningKey }
        val commandSigners = subscriptionState.participants.map { it.owningKey }
        val txCommand = Command(SubscriptionContract.Commands.Subscribe(ourIdentity, serviceProvider), commandSigners)
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(subscriptionState, SubscriptionContract.ID)
                .addCommand(txCommand)

        // Stage 3.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        // Verify that the transaction is valid.
        txBuilder.verify(serviceHub)

        // Stage 4.
        progressTracker.currentStep = SIGNING_TRANSACTION
        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Stage 5.
        // Oracle Sign

        // Stage 6.
        progressTracker.currentStep = GATHERING_SIGS

        // Send the state to the counterparty, and receive it back with their signature.
        val sessions = (subscriptionState.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))


        // Stage 7.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in all parties' vaults.
        return subFlow(FinalityFlow(fullySignedTx, sessions, FINALISING_TRANSACTION.childProgressTracker()))
    }
}
