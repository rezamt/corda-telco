package com.telco.contracts

import com.telco.states.SubscriptionState
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
                validateSubscribeTx(command, tx)
            }

            is Commands.UnSubscribe -> {
                validateUnSubscribeTx(command, tx)
            }

            is Commands.Approved -> {
                validateApprovalTx(command, tx)
            }

            is Commands.Connected -> {
                validateConnectedTx(command, tx)
            }

            is Commands.Activated -> {
                validateActivatedTx(command, tx)
            }

        }
    }


    fun validateSubscribeTx(command: CommandWithParties<CommandData>, tx: LedgerTransaction) {
        requireThat {

            // Validating IN/OUT Tx
            "New Subscriptions has not initial state attached to them" using (tx.inputStates.isEmpty())
            "New Subscriptions creates one and only one new output state" using (tx.outputStates.isNotEmpty() and (tx.outputStates.size == 1))

            // Validating Signers
            val subscriptionState = tx.outputStates.single() as SubscriptionState
            "Both Subscriber and Service Provider must sign and agree on this transaction" using
                    (command.signers.toSet() == subscriptionState.participants.map { it.owningKey }.toSet())

            // Validation State Fields
            "No Billing account should be assigned to this transaction" using (subscriptionState.billingAccountID.isEmpty())
            "Account is not active" using (subscriptionState.accountStatus.compareTo("InActive") == 0)

            // Validating OUs
            val subscribeCommand = command.value as Commands.Subscribe
            "Only Member of Service Providers are allowed to Subscribe service consumers" using (subscribeCommand.serviceProvider.name.organisationUnit?.compareTo("ServiceProviders") == 0)
            "Only Member of Service Subscribers are allowed to Subscribe service consumers" using (subscribeCommand.subscriber.name.organisationUnit?.compareTo("ServiceSubscriber") == 0)


            // todo: Check for Start and EndDate - Policy for at least 1 month
        }
    }

    fun validateUnSubscribeTx(command: CommandWithParties<CommandData>, tx: LedgerTransaction) {
        requireThat {
            val subscriptionState = tx.outputStates.single() as SubscriptionState
            "Both Subscriber and Service Provider must sign and agree on this transaction" using
                    (command.signers.toSet() == subscriptionState.participants.map { it.owningKey }.toSet())

            // Validation State Fields
            "No Billing account should be assigned to this transaction" using (subscriptionState.billingAccountID.isEmpty())
            "Account Status is not Inactive" using (subscriptionState.accountStatus.compareTo("InActive") == 0)
            "Service Status should be Pending" using (subscriptionState.serviceStatus.compareTo("PendingApproval") == 0)

            // Validating OUs
            val subscribeCommand = command.value as Commands.Subscribe
            "Only Member of Service Providers are allowed to Subscribe service consumers" using (subscribeCommand.serviceProvider.name.organisationUnit?.compareTo("ServiceProviders") == 0)
            "Only Member of Service Subscribers are allowed to Subscribe service consumers" using (subscribeCommand.subscriber.name.organisationUnit?.compareTo("ServiceSubscriber") == 0)
        }
    }

    fun validateApprovalTx(command: CommandWithParties<CommandData>, tx: LedgerTransaction) {
        requireThat {
            val subscriptionOutputState = tx.outputStates.single() as SubscriptionState
            val subscriptionInputState = tx.inputStates.single() as SubscriptionState

            "Both Subscriber and Service Provider must sign and agree on this transaction" using
                    (command.signers.toSet() == subscriptionOutputState.participants.map { it.owningKey }.toSet())

            // Validation Input State Fields
            "No Billing account should be assigned to this transaction" using (subscriptionInputState.billingAccountID.isEmpty())
            "Account Status is not InActive" using (subscriptionInputState.accountStatus.compareTo("InActive") == 0)
            "Service Status should be Pending" using (subscriptionInputState.serviceStatus.compareTo("PendingApproval") == 0)


            // Validation Output State Fields
            "No Billing account should be assigned to this transaction" using (subscriptionOutputState.billingAccountID.isEmpty())
            "Account Status is not InActive" using (subscriptionOutputState.accountStatus.compareTo("InActive") == 0)
            "Service Status should be Approved" using (subscriptionOutputState.serviceStatus.compareTo("Approved") == 0)

            // Validating OUs
            val approvalCommand = command.value as Commands.Approved
            "Only Member of Service Providers are allowed to Subscribe service consumers" using (approvalCommand.serviceProvider.name.organisationUnit?.compareTo("ServiceProviders") == 0)
            "Only Member of Service Subscribers are allowed to Subscribe service consumers" using (approvalCommand.subscriber.name.organisationUnit?.compareTo("ServiceSubscriber") == 0)
        }
    }

    fun validateConnectedTx(command: CommandWithParties<CommandData>, tx: LedgerTransaction) {
        requireThat {
            val subscriptionOutputState = tx.outputStates.single() as SubscriptionState
            val subscriptionInputState = tx.inputStates.single() as SubscriptionState

            "Both Subscriber and Service Provider must sign and agree on this transaction" using
                    (command.signers.toSet() == subscriptionOutputState.participants.map { it.owningKey }.toSet())

            // Validation Input State Fields

            "No Billing account should be assigned to this transaction" using (subscriptionInputState.billingAccountID.isEmpty())
            "Account Status is not InActive" using (subscriptionInputState.accountStatus.compareTo("InActive") == 0)
            "Service Status should be Approved" using (subscriptionInputState.serviceStatus.compareTo("Approved") == 0)

            // Validation Output State Fields
            "No Billing account should be assigned to this transaction" using (subscriptionOutputState.billingAccountID.isEmpty())
            "Account Status is not InActive" using (subscriptionOutputState.accountStatus.compareTo("InActive") == 0)
            "Service Status is not Connected" using (subscriptionOutputState.serviceStatus.compareTo("Connected") == 0)

            // Validating OUs
            val connectedCommand = command.value as Commands.Connected
            "Only Member of Service Providers are allowed to Subscribe service consumers" using (connectedCommand.serviceProvider.name.organisationUnit?.compareTo("ServiceProviders") == 0)
            "Only Member of Service Subscribers are allowed to Subscribe service consumers" using (connectedCommand.subscriber.name.organisationUnit?.compareTo("ServiceSubscriber") == 0)
        }
    }

    fun validateActivatedTx(command: CommandWithParties<CommandData>, tx: LedgerTransaction) {
        requireThat {
            val subscriptionOutputState = tx.outputStates.single() as SubscriptionState
            val subscriptionInputState = tx.inputStates.single() as SubscriptionState


            "Both Subscriber and Service Provider must sign and agree on this transaction" using
                    (command.signers.toSet() == subscriptionOutputState.participants.map { it.owningKey }.toSet())

            // Validation Input State Fields
            "No Billing account should be assigned to this transaction" using (subscriptionInputState.billingAccountID.isEmpty())
            "Account Status is not InActive" using (subscriptionInputState.accountStatus.compareTo("InActive") == 0)
            "Service Status should be Pending" using (subscriptionInputState.serviceStatus.compareTo("Connected") == 0)

            // Validation Input State Fields
            "Billing account should be assigned to this transaction" using (!subscriptionOutputState.billingAccountID.isEmpty())
            "Account is not active" using (subscriptionOutputState.accountStatus.compareTo("Active") == 0)


            // Validating OUs
            val activatedCommand = command.value as Commands.Activated
            "Only Member of Service Providers are allowed to Subscribe service consumers" using (activatedCommand.serviceProvider.name.organisationUnit?.compareTo("ServiceProviders") == 0)
            "Only Member of Service Subscribers are allowed to Subscribe service consumers" using (activatedCommand.subscriber.name.organisationUnit?.compareTo("ServiceSubscriber") == 0)

        }
    }

}