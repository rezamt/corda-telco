package com.telco.states


import com.telco.contracts.BillingContract
import com.telco.schema.InvoiceSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.time.LocalDate

/**
 * The state object recording invoice of billable hours from the contractor
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 */
@BelongsToContract(BillingContract::class)
data class InvoiceState(
        var invoiceID: UniqueIdentifier = UniqueIdentifier(),
        var customerID: String,
        var billingAccountID: String,
        var serviceType: String,
        var issueDate: LocalDate,
        var dueDate: LocalDate,
        var amount: Double,
        val status: String,
        val serviceProvider: Party,
        val customer: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) :
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(serviceProvider, customer)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is InvoiceSchemaV1 -> InvoiceSchemaV1.PersistentInvoice(
                    invoiceID.id,
                    serviceProvider.name.toString(),
                    customerID,
                    billingAccountID,
                    serviceType,
                    issueDate,
                    dueDate,
                    amount,
                    status,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(InvoiceSchemaV1)
}
